package com.tinnkm.rpc.client;

import com.tinnkm.rpc.common.RpcDecoder;
import com.tinnkm.rpc.common.RpcEncoder;
import com.tinnkm.rpc.common.RpcRequest;
import com.tinnkm.rpc.common.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.ClosedChannelException;

/**
 * Created by tinnkm on 2017/11/14.
 */
public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);

    private String host;
    private int port;
    private RpcResponse response;
    private final Object obj = new Object();
    public RpcClient(String host,int port){
        this.host = host;
        this.port = port;
    }

    /**
     * 连接服务端,发送消息
     * @param request
     * @return
     * @throws Exception
     */
    public RpcResponse send(RpcRequest request) throws Exception{
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        try {
            bootstrap.group(eventExecutors)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new RpcEncoder(RpcRequest.class))
                                    .addLast(new RpcDecoder(RpcResponse.class))
                                    .addLast(RpcClient.this);
                        }
                    }).option(ChannelOption.SO_KEEPALIVE,true);
            //连接服务器
            ChannelFuture future = bootstrap.connect(host, port).sync();
            //像服务器发送请求
            future.channel().writeAndFlush(request).sync();
            // 用线程等待的方式决定是否关闭连接
            // 其意义是：先在此阻塞，等待获取到服务端的返回后，被唤醒，从而关闭网络连接
            synchronized (obj) {
                obj.wait();
            }
            if (response != null) {
                future.channel().closeFuture().sync();
            }
            return response;
        } finally {
            eventExecutors.shutdownGracefully();
        }
    }

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        this.response = rpcResponse;
        synchronized (obj){
            obj.notifyAll();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("service caught a error ; {}",cause.getMessage());
        ctx.close();
    }
}
