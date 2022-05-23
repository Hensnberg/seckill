package com.xxxx.seckill.service;

import com.xxxx.seckill.pojo.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xxxx.seckill.vo.LoginVo;
import com.xxxx.seckill.vo.RespBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author heisenberg
 * @since 2022-01-23
 */
public interface UserService extends IService<User> {

    String login(HttpServletResponse response, LoginVo loginVo);

    RespBean dologin(HttpServletRequest request, HttpServletResponse response, LoginVo loginVo);

    User getUserByCookie(String ticket,HttpServletRequest request,HttpServletResponse response);

    public RespBean updatePassword(String userTicket,String password, HttpServletRequest request, HttpServletResponse response);
}
