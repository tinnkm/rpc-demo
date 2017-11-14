package com.tinnkm.rpc.simple.demo.service;

import com.tinnkm.rpc.client.RpcProxy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by tinnkm on 2017/11/14.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring.xml")
public class IHelloServiceTest {

    @Autowired
    private RpcProxy rpcProxy;

    @Test
    public void sayHelloTest(){
        IHelloService helloService = (IHelloService) rpcProxy.create(IHelloService.class);
        String tinnkm = helloService.sayHello("tinnkm");
        System.out.println(tinnkm);
    }
}
