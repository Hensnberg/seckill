package com.xxxx.seckill.service;

import com.xxxx.seckill.pojo.Order;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.vo.GoodsVo;
import com.xxxx.seckill.vo.OrderDetailVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author heisenberg
 * @since 2022-01-24
 */
public interface OrderService extends IService<Order> {

    Order seckill(User user, GoodsVo goodsVo);

    OrderDetailVo detail(Long orderId);

    String createPath(User user, Long goodsId);

    Boolean checkPath(User user, Long goodsId, String path);

    Boolean checkCatcha(User user, Long goodsId, String captcha);
}
