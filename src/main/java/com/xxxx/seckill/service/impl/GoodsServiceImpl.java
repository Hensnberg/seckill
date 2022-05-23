package com.xxxx.seckill.service.impl;

import com.xxxx.seckill.pojo.Goods;
import com.xxxx.seckill.mapper.GoodsMapper;
import com.xxxx.seckill.service.GoodsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxxx.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author heisenberg
 * @since 2022-01-24
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    @Autowired
    GoodsMapper goodsMapper;

    @Override
    public List<GoodsVo> findGoodsVo() {
        return goodsMapper.findGoodsVo();
    }

    @Override
    public GoodsVo findGoodsVoByGoodsId(long goodsId) {
        return goodsMapper.findGoodsVoByGoodsId(goodsId);
    }
}
