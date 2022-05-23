package com.xxxx.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.xxxx.seckill.SeckillDemoApplication;
import com.xxxx.seckill.exception.GlobalException;
import com.xxxx.seckill.pojo.Order;
import com.xxxx.seckill.mapper.OrderMapper;
import com.xxxx.seckill.pojo.SeckillGoods;
import com.xxxx.seckill.pojo.SeckillOrder;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.result.CodeMsg;
import com.xxxx.seckill.service.GoodsService;
import com.xxxx.seckill.service.OrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxxx.seckill.service.SeckillGoodsService;
import com.xxxx.seckill.service.SeckillOrderService;
import com.xxxx.seckill.utils.MD5Util;
import com.xxxx.seckill.utils.UUIDUtil;
import com.xxxx.seckill.vo.GoodsVo;
import com.xxxx.seckill.vo.OrderDetailVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author heisenberg
 * @since 2022-01-24
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    SeckillGoodsService seckillGoodsService;

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    SeckillOrderService seckillOrderService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    RedisTemplate redisTemplate;

    @Transactional
    @Override
    public Order seckill(User user, GoodsVo goodsVo) {
        SeckillGoods seckillGoods = seckillGoodsService.getOne(new QueryWrapper<SeckillGoods>().eq("goods_id", goodsVo.getId()));
        seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
        boolean result=seckillGoodsService.update(new UpdateWrapper<SeckillGoods>().setSql("stock_count="+
                "stock_count-1").eq("goods_id",goodsVo.getId()).gt("stock_count",0));
        if(seckillGoods.getStockCount()<1){
            redisTemplate.opsForValue().set("isStockEmpty:"+goodsVo.getId(),"0");
            return null;
        }
        Order order = new Order();
        order.setUserId(user.getId());
        order.setGoodsId(goodsVo.getId());
        order.setDeliveryAddrId(0L);
        order.setGoodsName(goodsVo.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(seckillGoods.getSeckillPrice());
        order.setOrderChannel(1);
        order.setStatus(0);
        order.setCreateDate(new Date());
        orderMapper.insert(order);

        SeckillOrder seckillOrder=new SeckillOrder();
        seckillOrder.setUserId(user.getId());
        seckillOrder.setGoodsId(goodsVo.getId());
        seckillOrder.setOrderId(order.getId());
        seckillOrderService.save(seckillOrder);
        redisTemplate.opsForValue().set("order:"+user.getId()+":"+goodsVo.getId(),seckillOrder);
        return order;
    }



    @Override
    public OrderDetailVo detail(Long orderId) {

        if(orderId==null){
            throw new GlobalException(CodeMsg.ORDER_NOT_EXIST);
        }
        Order order = orderMapper.selectById(orderId);
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(order.getGoodsId());
        OrderDetailVo orderDetailVo=new OrderDetailVo();
        orderDetailVo.setGoods(goodsVo);
        orderDetailVo.setOrder(order);
        return orderDetailVo;
    }

    @Override
    public String createPath(User user, Long goodsId) {
        String str= MD5Util.md5(UUIDUtil.uuid()+"123456");
        redisTemplate.opsForValue().set("seckillPath:"+user.getId()+":"+goodsId,str,60, TimeUnit.SECONDS);
        return str;
    }

    @Override
    public Boolean checkPath(User user, Long goodsId, String path) {
        if(user==null||goodsId<0|| StringUtils.isEmpty(path)){
            return false;
        }
        String redisPath =(String)redisTemplate.opsForValue().get("seckillPath:" + user.getId() + ":" + goodsId);
        return redisPath.equals(path);
    }

    @Override
    public Boolean checkCatcha(User user, Long goodsId, String captcha) {
        if(user==null||goodsId<0|| StringUtils.isEmpty(captcha)){
            return false;
        }
        String redisCaptcha = (String) redisTemplate.opsForValue().get("captcha:" + user.getId() + ":" + goodsId);
        return captcha.equals(redisCaptcha);
    }
}
