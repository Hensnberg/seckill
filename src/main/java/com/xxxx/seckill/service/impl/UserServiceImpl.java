package com.xxxx.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxxx.seckill.exception.GlobalException;
import com.xxxx.seckill.mapper.UserMapper;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.result.CodeMsg;
import com.xxxx.seckill.service.UserService;
import com.xxxx.seckill.utils.CookieUtils;
import com.xxxx.seckill.utils.MD5Util;
import com.xxxx.seckill.utils.UUIDUtil;
import com.xxxx.seckill.vo.LoginVo;
import com.xxxx.seckill.vo.RespBean;
import com.xxxx.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author heisenberg
 * @since 2022-01-23
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    RedisTemplate redisTemplate;


    @Override
    public String login(HttpServletResponse response, LoginVo loginVo) {
        return null;
    }

    @Override
    public RespBean dologin(HttpServletRequest request, HttpServletResponse response, LoginVo loginVo) {
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();
        User user = userMapper.selectById(mobile);
        if(user==null){
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        if(!MD5Util.fromPassToDBPass(password,user.getSalt()).equals(user.getPassword())){
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        //生成Cookie
        String ticket= UUIDUtil.uuid();
//        request.getSession().setAttribute(ticket,user);
        redisTemplate.opsForValue().set("user:"+ticket,user);
        CookieUtils.setCookie(request,response,"userTicket",ticket);
        return RespBean.success(ticket);
    }

    @Override
    public User getUserByCookie(String ticket, HttpServletRequest request, HttpServletResponse response) {
        if(StringUtils.isEmpty(ticket)){
            return null;
        }
        User user = (User) redisTemplate.opsForValue().get("user:" + ticket);
        if(user!=null){
            CookieUtils.setCookie(request,response,"userTicket",ticket);
        }
        return user;
    }
    /**
     * 典型缓存同步场景：更新密码
     */
    @Override
    public RespBean updatePassword(String userTicket,String password, HttpServletRequest request, HttpServletResponse response) {
        //取user
        User user = getUserByCookie(userTicket,request,response);
        if(user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        user.setPassword(MD5Util.fromPassToDBPass(password, user.getSalt()));
        int result = userMapper.updateById(user);
        if(1==result){
            redisTemplate.delete("user:"+userTicket);
            return RespBean.success();
        }
        return RespBean.error(RespBeanEnum.PASSWORD_UPDATE_FAIL);
    }
}
