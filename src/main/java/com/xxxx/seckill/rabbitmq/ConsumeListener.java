package com.xxxx.seckill.rabbitmq;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.xxxx.seckill.pojo.SeckillMessage;
import com.xxxx.seckill.service.SeckillOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.xxxx.seckill.config.RabbitMQConfig.SECKILL_ORDER_KEY;

@Slf4j
@Component
public class ConsumeListener {
    @Autowired
    private SeckillOrderService secKillOrderService;
    @RabbitListener(queues = SECKILL_ORDER_KEY)
    public void receiveSecKillOrderMessage(Channel channel,Message message){
        log.info("接收消息："+message);
        //转换消息
        SeckillMessage seckillMessage = JSON.parseObject(message.getBody(),
                SeckillMessage.class);
        //同步mysql订单
        Boolean flag = secKillOrderService.createOrder(seckillMessage);
        //System.out.println("<>>><>><><><><><>><><><><><>>");
        System.out.println(seckillMessage.toString());
        if (flag){
            //返回成功通知
            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            //返回失败通知
            try {
                //第一个boolean true所有消费者都会拒绝这个消息，false代表只有当前消费者拒绝
                //第二个boolean true当前消息会进入到死信队列，false重新回到原有队列中，默认回到头部
                channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
