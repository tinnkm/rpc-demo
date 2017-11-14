package com.tinnkm.rpc.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by tinnkm on 2017/11/14.
 */
public class RpcEncoder extends MessageToByteEncoder {
    private Class<?> genericClass;
    public RpcEncoder(Class<?> genericClass){
        this.genericClass = genericClass;
    }
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        if (genericClass.isInstance(o)){
            byte[] serialize = SerializationUtil.serialize(o);
            byteBuf.writeInt(serialize.length);
            byteBuf.writeBytes(serialize);

        }
    }
}
