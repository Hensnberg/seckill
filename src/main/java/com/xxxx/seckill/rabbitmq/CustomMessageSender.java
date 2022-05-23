package com.xxxx.seckill.rabbitmq;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class CustomMessageSender implements RabbitTemplate.ConfirmCallback {

    static final Logger log = LoggerFactory.getLogger(CustomMessageSender.class);
    private static final String MESSAGE_CONFIRM="message_confirm";

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RedisTemplate redisTemplate;

    public CustomMessageSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        rabbitTemplate.setConfirmCallback(this);
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack){
            //返回成功通知
            //删除redis中的相关数据
            redisTemplate.delete(correlationData.getId());
            redisTemplate.delete(MESSAGE_CONFIRM+correlationData.getId());
            //System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        }else{
            //返回失败通知
            Map<String,String> map = (Map<String,String>)redisTemplate.opsForHash().entries(MESSAGE_CONFIRM+correlationData.getId());
            String exchange = map.get("exchange");
            String routingKey = map.get("routingKey");
            String sendMessage = map.get("sendMessage");
            //重新发送
            //System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            rabbitTemplate.convertAndSend(exchange,routingKey,
                    JSON.toJSONString(sendMessage));
        }
    }

    public void sendMessage(String exchange,String routingKey,String message){
        CorrelationData correlationData=new CorrelationData(UUID.randomUUID().toString());
        redisTemplate.opsForValue().set(correlationData.getId(),message);
        Map<String,String> map=new HashMap<>();
        map.put("exchange", exchange);
        map.put("routingKey", routingKey);
        map.put("sendMessage", message);
        redisTemplate.opsForHash().putAll(MESSAGE_CONFIRM+correlationData.getId(),map);
        rabbitTemplate.convertAndSend(exchange,routingKey,message,correlationData);
    }
}
