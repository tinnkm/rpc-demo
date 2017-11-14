package com.tinnkm.rpc.common;


import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by tinnkm on 2017/11/14.
 */
public class SerializationUtil {
    private static Map<Class<?>,Schema<?>> cachedSchema = new ConcurrentHashMap<Class<?>, Schema<?>>();
    private static Objenesis objenesis = new ObjenesisStd(true);

    /**
     * 获取类的schema
     * @param cls
     * @param <T>
     * @return
     */
    private static <T> Schema<T> getSchema(Class<T> cls){
        Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
        if (null == schema){
            schema = RuntimeSchema.createFrom(cls);
            if (schema != null){
                cachedSchema.put(cls,schema);
            }
        }
        return schema;
    }

    /**
     * 将对象序列化为字节数组
     * @param obj
     * @param <T>
     * @return
     */
    public static  <T> byte[] serialize(T obj){
        Class<T> cls = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(cls);
            return ProtostuffIOUtil.toByteArray(obj,schema,buffer);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage(),e);
        }finally {
            buffer.clear();
        }
    }

    /**
     * 将字节数组反序列化为对象
     * @param data
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> T deSerialize(byte[] data,Class<T> cls){
        try {
            /*
        	 * 如果一个类没有参数为空的构造方法时候，那么你直接调用newInstance方法试图得到一个实例对象的时候是会抛出异常的
        	 * 通过ObjenesisStd可以完美的避开这个问题
        	 * */
            T message = objenesis.newInstance(cls);//实例化
            Schema<T> schema = getSchema(cls);
            ProtostuffIOUtil.mergeFrom(data,message,schema);
            return message;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(),e);
        }


    }
}
