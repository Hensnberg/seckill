package com.xxxx.seckill.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pig4cloud.captcha.ArithmeticCaptcha;
import com.xxxx.seckill.config.AccessLimit;
import com.xxxx.seckill.exception.GlobalException;
import com.xxxx.seckill.pojo.*;
import com.xxxx.seckill.rabbitmq.CustomMessageSender;
import com.xxxx.seckill.rabbitmq.MQSender;
import com.xxxx.seckill.result.CodeMsg;
import com.xxxx.seckill.service.GoodsService;
import com.xxxx.seckill.service.OrderService;
import com.xxxx.seckill.service.SeckillOrderService;
import com.xxxx.seckill.utils.CookieUtils;
import com.xxxx.seckill.vo.GoodsVo;
import com.xxxx.seckill.vo.RespBean;
import com.xxxx.seckill.vo.RespBeanEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.xxxx.seckill.config.RabbitMQConfig.SECKILL_ORDER_KEY;

@Slf4j
@Controller
@RequestMapping("/seckill")
public class SeckillController implements InitializingBean {

    @Autowired
    GoodsService goodsService;

    @Autowired
    SeckillOrderService seckillOrderService;

    @Autowired
    OrderService orderService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    MQSender mqSender;

    @Autowired
    RedisScript<Long> script;

    @Autowired
    CustomMessageSender customMessageSender;

    private Map<Long,Boolean> EmptyStockMap=new HashMap<>();


//    @RequestMapping("/do_seckill")
//    public String doSeckill(Model model, User user,Long goodsId){
//        if(user==null){
//            return "login";
//        }
//        model.addAttribute("user",user);
//        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
//        if(goodsVo.getStockCount()<1){
//            model.addAttribute("errmsg", CodeMsg.ORDER_NOT_EXIST);
//            return "seckill_fail";
//        }
//        QueryWrapper<SeckillOrder>query=new QueryWrapper<>();
//        query.eq("user_id",user.getId());
//        query.eq("goods_id",goodsVo.getId());
//        SeckillOrder seckillOrder = seckillOrderService.getOne(query);
//        if(seckillOrder!=null){
//            model.addAttribute("errmsg",CodeMsg.REPEATE_SECKILL);
//            return "seckill_fail";
//        }
//        Order order=orderService.seckill(user,goodsVo);
//        model.addAttribute("orderInfo",order);
//        model.addAttribute("goods",goodsVo);
//        return "order_detail";
//    }

    //获取秒杀结果
    @GetMapping("/result")
    @ResponseBody
    public RespBean getResult(User user,Long goodsId){
        if(user==null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Long orderId=seckillOrderService.getResult(user,goodsId);
        return RespBean.success(orderId);
    }

    //隐藏路径
    @AccessLimit(second=5,maxCount=5,needLogin=true)
    @GetMapping("/path")
    @ResponseBody
    public RespBean getPath(User user, Long goodsId, String captcha){  //, HttpServletRequest request
        if(user==null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
//        String uri=request.getRequestURI();
        captcha="0";
//        Integer count=(Integer)redisTemplate.opsForValue().get(uri+":"+user.getId());
//        if(count==null){
//            redisTemplate.opsForValue().set(uri+":"+user.getId(),1,5,TimeUnit.SECONDS);
//        }else if(count<5){
//            redisTemplate.opsForValue().increment(uri+":"+user.getId());
//        }else{
//            return RespBean.error(RespBeanEnum.ACCESS_LIMIT_REAHCED);
//        }
        Boolean check=orderService.checkCatcha(user,goodsId,captcha);
        if(!check){
            return RespBean.error(RespBeanEnum.ERROR_CAPTCHA);
        }
        String path=orderService.createPath(user,goodsId);
        return RespBean.success(path);
    }

    @GetMapping("/captcha")
    public void verifyCode(User user, Long goodsId, HttpServletResponse response) {
        if(user==null||goodsId<0){
            throw new GlobalException(CodeMsg.SESSION_ERROR);
        }
        //设置请求头为输出图片的类型
        response.setContentType("image/gif");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        //生成验证码，将结果放入Redis
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 30,3);
        redisTemplate.opsForValue().set("captcha:"+user.getId()+":"+goodsId,captcha.text(),300, TimeUnit.SECONDS);
        try {
            captcha.out(response.getOutputStream());
        } catch (IOException e) {
            log.error("验证码生成失败",e.getMessage());
        }

    }

    @PostMapping("/{path}/doseckill")
    @ResponseBody
    public RespBean doSeckill(@PathVariable String path, User user, Long goodsId){
        if(user==null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Boolean check=orderService.checkPath(user,goodsId,path);
        if(!check){
            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if(seckillOrder!=null){
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }
        //内存标记减少Redis访问
        if(EmptyStockMap.get(goodsId)){
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        //redis预减库存
        //Long stock = redisTemplate.opsForValue().decrement("seckillGoods:" + goodsId);
        Long stock = (Long) redisTemplate.execute(script, Collections.singletonList("seckillGoods:" + goodsId), Collections.EMPTY_LIST);
        if(stock<0){
            EmptyStockMap.put(goodsId,true);
            redisTemplate.opsForValue().increment("seckillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
        //mqSender.sendSEckillMessage(JSON.toJSONString(seckillMessage));
        customMessageSender.sendMessage("",SECKILL_ORDER_KEY,JSON.toJSONString(seckillMessage));
        return RespBean.success(0);


        //GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);


//        if(goodsVo.getStockCount()<1){
////            model.addAttribute("errmsg", CodeMsg.ORDER_NOT_EXIST);
//            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
//        }
////        QueryWrapper<SeckillOrder>query=new QueryWrapper<>();
////        query.eq("user_id",user.getId());
////        query.eq("goods_id",goodsVo.getId());
////        SeckillOrder seckillOrder = seckillOrderService.getOne(query);
//        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsVo.getId());
//        if(seckillOrder!=null){
////            model.addAttribute("errmsg",CodeMsg.REPEATE_SECKILL);
//            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
//        }
//        Order order=orderService.seckill(user,goodsVo);
//        return RespBean.success(order);
    }


    //系统初始化，把商品库存数量加载到Redis
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> list=goodsService.findGoodsVo();
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        list.forEach(goodsVo -> {
            redisTemplate.opsForValue().set("seckillGoods:"+goodsVo.getId(),goodsVo.getStockCount());
            EmptyStockMap.put(goodsVo.getId(),false);
            }
        );
    }
}
