package com.xxxx.seckill.vo;
import com.xxxx.seckill.pojo.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by jiangyunxiong on 2018/5/28.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailVo {
    private GoodsVo goods;
    private Order order;
}
