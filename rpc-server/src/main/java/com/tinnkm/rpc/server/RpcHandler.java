package com.tinnkm.rpc.server;

import com.tinnkm.rpc.common.RpcRequest;
import com.tinnkm.rpc.common.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by tinnkm on 2017/11/14.
 */
public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcHandler.class);
    private final Map<String,Object> handlerMap;

    public RpcHandler(Map<String,Object> handlerMap){
        this.handlerMap = handlerMap;
    }

    /**
     * 接收消息
     * @param channelHandlerContext 上下文处理类
     * @param rpcRequest 请求体
     * @throws Exception
     */
    @Override
    protected void channelRead0(final ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setRequestId(rpcRequest.getRequestId());

        try {
            //处理业务调用
            Object result = handle(rpcRequest);
            rpcResponse.setResult(result);
        } catch (Throwable throwable) {
            rpcResponse.setError(throwable);
        }
        //写入到outbundle进行下一步的处理
        channelHandlerContext.writeAndFlush(rpcResponse).addListener(ChannelFutureListener.CLOSE);

    }

    private Object handle(RpcRequest request) throws Throwable{
        String className = request.getClassName();
        //拿到实现类对象
        Object serviceBean = handlerMap.get(className);
        System.out.println(serviceBean);
        //拿到要调用的方法名\参数类型\参数值
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        //拿到接口类
        Class<?> forName = Class.forName(className);

        //调用实现类对象的指定方法并返回结果
        Method method = forName.getMethod(methodName, parameterTypes);
        return method.invoke(serviceBean,parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("service caught exception ; {}",cause);
        ctx.close();
    }
}
