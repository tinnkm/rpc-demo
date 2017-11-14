package com.tinnkm.rpc.simple.demo.bootstrap;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by tinnkm on 2017/11/14.
 */
public class BootStrap {
    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("spring.xml");
    }
}
