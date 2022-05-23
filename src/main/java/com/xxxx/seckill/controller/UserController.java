package com.xxxx.seckill.controller;


import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.rabbitmq.MQSender;
import com.xxxx.seckill.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author heisenberg
 * @since 2022-01-23
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    MQSender mqSender;

    @RequestMapping("/info")
    @ResponseBody
    public RespBean info(User user){
        return RespBean.success(user);
    }

//    //测试发送mq
//    @RequestMapping("/mq")
//    @ResponseBody
//    public void mq(){
//        mqSender.send("Hello");
//    }
//
//    //测试发送mq
//    @RequestMapping("/mq/fanout")
//    @ResponseBody
//    public void fanout(){
//        mqSender.send("Hello");
//    }
//
//    //测试发送mq
//    @RequestMapping("/mq/direct01")
//    @ResponseBody
//    public void direct01(){
//        mqSender.send01("Hello,Red!");
//    }
//
//    //测试发送mq
//    @RequestMapping("/mq/direct02")
//    @ResponseBody
//    public void direct02(){
//        mqSender.send02("Hello,Green!");
//    }
//
//    @RequestMapping("/mq/topic01")
//    @ResponseBody
//    public void topict01(){
//        mqSender.send03("Hello,Red!");
//    }
//
//    @RequestMapping("/mq/topic02")
//    @ResponseBody
//    public void topict02(){
//        mqSender.send04("Hello,Green!");
//    }
}
