package com.xxxx.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 公共返回对象的枚举
 */
@Getter
@ToString
@AllArgsConstructor
public enum RespBeanEnum {

    //通用
    SUCCESS(200,"SUCCESS"),
    ERROR(500,"服务端异常"),
    //登录模块
    LOGIN_ERROR(500210,"用户名或密码错误"),
    MOBILE_ERROR(500211,"手机号码格式不正确"),
    PASSWORD_UPDATE_FAIL(500212,"手机密码更新失败"),
    SESSION_ERROR(500213,"登陆异常"),
    EMPTY_STOCK(500214,"库存为空"),
    REPEATE_ERROR(500215,"不能重复秒杀"),
    REQUEST_ILLEGAL(500216,"请求非法，请重新尝试"),
    ERROR_CAPTCHA(500217,"验证码错误，请重新输入"),
    ACCESS_LIMIT_REAHCED(500218,"秒杀次数过多，请稍后再试")

    ;
    private final Integer code;
    private final String message;
}

