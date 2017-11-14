package com.tinnkm.rpc.registry;

import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by tinnkm on 2017/11/14.
 */
public class ServiceRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistry.class);
    private CountDownLatch latch = new CountDownLatch(1);
    //zookeeper的地址
    private String registryAddress;

    public ServiceRegistry(String registryAddress){
        this.registryAddress = registryAddress;
    }

    /**
     * 将数据存入zookeeper
     * @param data
     */
    public void register(String data){
        if (data != null){
            ZooKeeper zk = connectServer();
            if (zk != null){
                createNode(zk,data);
            }
        }
    }

    /**
     * 创建数据节点
     * @param zk
     * @param data
     */
    private void createNode(ZooKeeper zk, String data) {
        byte[] bytes = data.getBytes();
        try {
            if (zk.exists(Constant.ZK_REGISTRY_PATH,null) == null){
                //如果根节点不存在创建根节点
                zk.create(Constant.ZK_REGISTRY_PATH,null, ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
            }
            //创建数据节点,并存储数据
            String path = zk.create(Constant.ZK_DATA_PATH,bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);
            LOGGER.debug("create zookeeper node ({} => {})",path,data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取zookeeper连接
     * @return
     */
    public ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getState() == Event.KeeperState.SyncConnected){
                        latch.countDown();
                    }
                }
            });
            latch.await();
        } catch (Exception e) {
            LOGGER.error("connect to zookeeper failed : {}",e.getMessage());
        }
        return zk;
    }

}
