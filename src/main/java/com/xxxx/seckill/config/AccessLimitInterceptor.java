package com.xxxx.seckill.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.service.UserService;
import com.xxxx.seckill.utils.CookieUtils;
import com.xxxx.seckill.vo.RespBean;
import com.xxxx.seckill.vo.RespBeanEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.HandlerMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;


@Component
@Slf4j
public class AccessLimitInterceptor implements HandlerInterceptor {

    @Autowired
    UserService userService;

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(handler instanceof HandlerMethod){
            User user=getUser(request,response);
//            log.info(user.toString());
//            UserContext.setUser(user);
            HandlerMethod hm=(HandlerMethod)handler;
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            if(accessLimit==null){
                return true;
            }
            int second=accessLimit.second();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();
            String key = request.getRequestURI();
            if(needLogin){
                if(user==null){
                    render(response, RespBeanEnum.SESSION_ERROR);
                    return false;
                }
                key+=":"+user.getId();
            }
            Integer count=(Integer)redisTemplate.opsForValue().get(key);
            if(count==null){
                redisTemplate.opsForValue().set(key,1,second, TimeUnit.SECONDS);
            }else if(count<maxCount){
                redisTemplate.opsForValue().increment(key);
            }else{
                render(response,RespBeanEnum.ACCESS_LIMIT_REAHCED);
                return false;
            }

        }
        return true;
    }

    //构建返回对象
    private void render(HttpServletResponse response,RespBeanEnum respBeanEnum) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out=response.getWriter();
        RespBean respBean=RespBean.error(respBeanEnum);
        out.write(new ObjectMapper().writeValueAsString(respBean));
        out.flush();
        out.close();

    }

    private User getUser(HttpServletRequest request,HttpServletResponse response){
        String ticket= CookieUtils.getCookieValue(request,"userTicket");
        if(StringUtils.isEmpty(ticket)){
            return null;
        }
        return userService.getUserByCookie(ticket,request,response);
    }
}
