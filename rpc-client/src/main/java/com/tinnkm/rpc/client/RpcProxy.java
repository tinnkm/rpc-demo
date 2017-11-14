package com.tinnkm.rpc.client;

import com.tinnkm.rpc.common.RpcRequest;
import com.tinnkm.rpc.common.RpcResponse;
import com.tinnkm.rpc.registry.ServiceDiscovery;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * Rpc代理
 * Created by tinnkm on 2017/11/14.
 */
public class RpcProxy {
    private String serverAddress;
    private ServiceDiscovery serviceDiscovery;

    public RpcProxy(ServiceDiscovery serviceDiscovery){
        this.serviceDiscovery = serviceDiscovery;
    }

    public <T> Object create(Class<T> interfaceClass){
        return Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass}, new InvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        //创建rpcRequest,封装被代理类的属性
                        RpcRequest request = new RpcRequest();
                        request.setRequestId(UUID.randomUUID().toString());
                        request.setClassName(method.getDeclaringClass().getName());
                        request.setMethodName(method.getName());
                        request.setParameterTypes(method.getParameterTypes());
                        request.setParameters(args);
                        //查找服务
                        if (serviceDiscovery != null){
                            serverAddress = serviceDiscovery.discover();
                        }
                        String[] split = serverAddress.split(":");
                        String host = split[0];
                        int port = Integer.parseInt(split[1]);
                        RpcClient client = new RpcClient(host, port);
                        //像服务端请求数据
                        RpcResponse response = client.send(request);
                        if (response.isError()){
                            throw response.getError();
                        }else{
                            return response.getResult();
                        }

                    }
                });
    }
}
