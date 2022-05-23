package com.xxxx.seckill.service;

import com.xxxx.seckill.pojo.Goods;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xxxx.seckill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author heisenberg
 * @since 2022-01-24
 */
public interface GoodsService extends IService<Goods> {

    List<GoodsVo> findGoodsVo();

    GoodsVo findGoodsVoByGoodsId(long goodsId);
}
