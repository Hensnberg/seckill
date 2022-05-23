package com.xxxx.seckill.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String SECKILL_ORDER_KEY="seckillQueue";
    public static final String EXCHANGE="seckillExchange";
    public static final String ROUTINGKEY="queue.seckill";
    @Bean
    public Queue queue(){
        //开启队列持久化
        return new Queue(SECKILL_ORDER_KEY,true);
    }
//    @Bean
//    public TopicExchange topicExchange(){
//        return new TopicExchange(EXCHANGE);
//    }
//
//    @Bean
//    public Binding binding(){
//        return BindingBuilder.bind(queue()).to(topicExchange()).with(ROUTINGKEY);
//    }

}
