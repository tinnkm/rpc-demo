package com.tinnkm.rpc.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * rpc解码
 * Created by tinnkm on 2017/11/14.
 */
public class RpcDecoder extends ByteToMessageDecoder {
    private Class<?> genericClass;
    //构造函数传入反序列化的class
    public RpcDecoder(Class<?> genericClass){
        this.genericClass = genericClass;
    }
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        //如果是消息,则不处理
        if (byteBuf.readableBytes() < 4){
            return;
        }
        byteBuf.markReaderIndex();
        int dataLength = byteBuf.readInt();
        if (dataLength < 0){
            channelHandlerContext.close();
        }
        if (byteBuf.readableBytes() < dataLength){
            byteBuf.resetReaderIndex();
        }
        byte[] bytes = new byte[dataLength];
        //将数据写入bytes中
        byteBuf.readBytes(bytes);
        Object obj = SerializationUtil.deSerialize(bytes, genericClass);
        list.add(obj);
    }
}
