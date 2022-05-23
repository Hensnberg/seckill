package com.xxxx.seckill.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//运行时就产生
@Retention(RetentionPolicy.RUNTIME)
//放在方法上
@Target(ElementType.METHOD)
public @interface AccessLimit {

    int second();
    int maxCount();
    boolean needLogin() default true;
}
