package com.tinnkm.rpc.simple.demo.service.impl;

import com.tinnkm.rpc.server.RpcService;
import com.tinnkm.rpc.simple.demo.service.IHelloService;

/**
 * Created by tinnkm on 2017/11/14.
 */
@RpcService(IHelloService.class)
public class HelloServiceImpl implements IHelloService {
    public String sayHello(String name) {
        return name+",hello!";
    }
}
