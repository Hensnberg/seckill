package com.xxxx.seckill.controller;

import com.xxxx.seckill.result.Result;
import com.xxxx.seckill.service.UserService;
import com.xxxx.seckill.vo.LoginVo;
import com.xxxx.seckill.vo.RespBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequestMapping("/login")
@Slf4j
public class LoginController {

    @Autowired
    UserService userService;

    @RequestMapping("/toLogin")
    public String toLogin(){
        return "login";
    }

    @RequestMapping("/dologin")
    @ResponseBody
    public Result<String> doLogin(HttpServletResponse response, HttpServletRequest request, @Valid LoginVo loginVo) {//加入JSR303参数校验
//        log.info(loginVo.toString());
        log.info(loginVo.toString());
//        String token = userService.login(response, loginVo);
        RespBean pesp=userService.dologin(request,response,loginVo);

        log.info(pesp.toString());
        return Result.success(pesp.toString());
    }
}
