package com.xxxx.seckill.vo;


import com.xxxx.seckill.pojo.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by jiangyunxiong on 2018/5/24.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetailVo {
    private int seckillStatus = 0;
    private int remainSeconds = 0;
    private GoodsVo goods ;
    private User user;
}
