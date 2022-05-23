package com.xxxx.seckill.service;

import com.xxxx.seckill.pojo.SeckillMessage;
import com.xxxx.seckill.pojo.SeckillOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xxxx.seckill.pojo.User;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author heisenberg
 * @since 2022-01-24
 */
public interface SeckillOrderService extends IService<SeckillOrder> {

    Long getResult(User user, Long goodsId);

    Boolean createOrder(SeckillMessage seckillMessage);
}
