package com.tinnkm.rpc.server;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by tinnkm on 2017/11/14.
 */
@Target({ElementType.TYPE}) //注解作用再类上
@Retention(RetentionPolicy.RUNTIME)//vm在运行时保留注释
@Component//让这个注解可以被spring扫描到
public @interface RpcService {
    Class<?> value();
}
