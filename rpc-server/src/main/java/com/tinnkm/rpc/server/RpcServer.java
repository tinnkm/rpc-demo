package com.tinnkm.rpc.server;

import com.tinnkm.rpc.common.RpcDecoder;
import com.tinnkm.rpc.common.RpcEncoder;
import com.tinnkm.rpc.common.RpcRequest;
import com.tinnkm.rpc.common.RpcResponse;
import com.tinnkm.rpc.registry.ServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;


/**
 * rpc服务类
 * 实现applicationContextAware和InitializinBean
 *
 * Created by tinnkm on 2017/11/14.
 */
public class RpcServer implements ApplicationContextAware,InitializingBean{
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);
    private String serverAddress;
    private ServiceRegistry serviceRegistry;
    //用于存储业务接口和实现类的实例对象
    private Map<String,Object> handlerMap = new HashMap<String, Object>();
    public RpcServer(String serverAddress){
        this.serverAddress = serverAddress;
    }
    //服务器绑定地址和端口
    public RpcServer(String serverAddress,ServiceRegistry serviceRegistry){
        this.serverAddress = serverAddress;
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * 再bean被创建完成后触发
     * 启动netty服务,绑定handle流水线
     * @throws Exception
     */
    public void afterPropertiesSet() throws Exception {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new RpcEncoder(RpcResponse.class))//out
                                    .addLast(new RpcDecoder(RpcRequest.class))//in
                                    .addLast(new RpcHandler(handlerMap));
                        }
                    }).option(ChannelOption.SO_BACKLOG,128).childOption(ChannelOption.SO_KEEPALIVE,true);
            String[] array = serverAddress.split(":");
            String host = array[0];
            int port = Integer.parseInt(array[1]);

            ChannelFuture future = serverBootstrap.bind(host, port).sync();
            LOGGER.debug("service started on port {}",port);
            if (serviceRegistry != null){
                serviceRegistry.register(serverAddress);
            }
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }


    }

    //通过注解,获取标注了Rpc服务注解的业务类将
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(RpcService.class);
        if (MapUtils.isNotEmpty(beansWithAnnotation)){
            for (Object serviceBean : beansWithAnnotation.values()){
                String interfaceName = serviceBean.getClass().getAnnotation(RpcService.class).value().getName();
                handlerMap.put(interfaceName,serviceBean);
            }
        }
    }
}
