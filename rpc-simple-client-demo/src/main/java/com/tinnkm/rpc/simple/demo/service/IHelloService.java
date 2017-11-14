package com.tinnkm.rpc.simple.demo.service;

/**
 * Created by tinnkm on 2017/11/14.
 * 此接口为远程服务接口,接口路径需要与远程服务路径一致,否则无法获取到实现类
 */
public interface IHelloService{
    String sayHello(String name);
}