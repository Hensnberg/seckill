package com.xxxx.seckill.controller;


import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.service.GoodsService;
import com.xxxx.seckill.service.UserService;
import com.xxxx.seckill.vo.DetailVo;
import com.xxxx.seckill.vo.GoodsVo;
import com.xxxx.seckill.vo.RespBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
//import org.thymeleaf.spring4.context.SpringWebContext;
//import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiangyunxiong on 2018/5/22.
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    UserService userService;

//    @Autowired
//    RedisService redisService;
//
    @Autowired
    GoodsService goodsService;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;


//    @Autowired
//    ThymeleafViewResolver thymeleafViewResolver;

    /**
     * 商品列表页面
     * QPS:433
     * 1000 * 10
     */
    @RequestMapping(value = "/to_list", produces = "text/html;charset=utf-8") //
    @ResponseBody
    public String list(Model model, User user,HttpServletRequest request,HttpServletResponse response) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html = (String) valueOperations.get("goods_list");
        if(!StringUtils.isEmpty(html)){
            return html;
        }
        model.addAttribute("user",user);
        model.addAttribute("goodsList",goodsService.findGoodsVo());

        WebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goods_list", context);
        if(!StringUtils.isEmpty(html)){
            valueOperations.set("goods_list",html,60, TimeUnit.SECONDS);
        }
        //return "goods_list";
        return html;
    }


    /**
     * 商品详情页面
     */
//    @RequestMapping(value = "/toDetail/{goodsId}", produces = "text/html;charset=utf-8")
//    @ResponseBody
//    public String detail(Model model, User user, @PathVariable("goodsId") long goodsId,HttpServletRequest request,HttpServletResponse response) {
//        ValueOperations valueOperations = redisTemplate.opsForValue();
//        String html = (String) valueOperations.get("goods_detail:"+goodsId);
//        if(!StringUtils.isEmpty(html)){
//            return html;
//        }
//        model.addAttribute("user", user);
//        GoodsVo goodsVo= goodsService.findGoodsVoByGoodsId(goodsId);
//        Date startDate = goodsVo.getStartDate();
//        Date endDate = goodsVo.getEndDate();
//        Date nowDate=new Date();
//        int seckillStatus=0;
//        int remainSeconds=0;
//        if(nowDate.before(startDate)){
//            remainSeconds=(int)((startDate.getTime()-nowDate.getTime())/1000);
//        }else if(nowDate.after(endDate)){
//            seckillStatus=2;
//            remainSeconds=-1;
//        }else{
//            seckillStatus=1;
//            remainSeconds=0;
//        }
//        model.addAttribute("remainSeconds",remainSeconds);
//        model.addAttribute("seckillStatus",seckillStatus);
//        model.addAttribute("goods",goodsVo);
//        WebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
//        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", context);
//        if(!StringUtils.isEmpty(html)){
//            valueOperations.set("goods_detail:"+goodsId,html,60, TimeUnit.MINUTES);
//        }
//        //return "goods_list";
//        return html;
//    }

    /**
     * 商品详情页面
     */
    @RequestMapping(value = "/detail/{goodsId}")
    @ResponseBody
    public RespBean detail2(User user, @PathVariable("goodsId") long goodsId) {
        GoodsVo goodsVo= goodsService.findGoodsVoByGoodsId(goodsId);
        Date startDate = goodsVo.getStartDate();
        Date endDate = goodsVo.getEndDate();
        Date nowDate=new Date();
        int seckillStatus=0;
        int remainSeconds=0;
        if(nowDate.before(startDate)){
            remainSeconds=(int)((startDate.getTime()-nowDate.getTime())/1000);
        }else if(nowDate.after(endDate)){
            seckillStatus=2;
            remainSeconds=-1;
        }else{
            seckillStatus=1;
            remainSeconds=0;
        }
        DetailVo detailVo=new DetailVo();
        detailVo.setUser(user);
        detailVo.setGoods(goodsVo);
        detailVo.setRemainSeconds(remainSeconds);
        detailVo.setSeckillStatus(seckillStatus);

        return RespBean.success(detailVo);
    }
//
//    /**
//     * 商品详情页面
//     */
//    @RequestMapping(value = "/detail/{goodsId}")
//    @ResponseBody
//    public Result<GoodsDetailVo> detail(HttpServletRequest request, HttpServletResponse response, Model model, User user, @PathVariable("goodsId") long goodsId) {
//
//        //根据id查询商品详情
//        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
//        model.addAttribute("goods", goods);
//
//        long startTime = goods.getStartDate().getTime();
//        long endTime = goods.getEndDate().getTime();
//        long now = System.currentTimeMillis();
//
//        int seckillStatus = 0;
//        int remainSeconds = 0;
//
//        if (now < startTime) {//秒杀还没开始，倒计时
//            seckillStatus = 0;
//            remainSeconds = (int) ((startTime - now) / 1000);
//        } else if (now > endTime) {//秒杀已经结束
//            seckillStatus = 2;
//            remainSeconds = -1;
//        } else {//秒杀进行中
//            seckillStatus = 1;
//            remainSeconds = 0;
//        }
//        GoodsDetailVo vo = new GoodsDetailVo();
//        vo.setGoods(goods);
//        vo.setUser(user);
//        vo.setRemainSeconds(remainSeconds);
//        vo.setSeckillStatus(seckillStatus);
//
//        return Result.success(vo);
//    }
}
