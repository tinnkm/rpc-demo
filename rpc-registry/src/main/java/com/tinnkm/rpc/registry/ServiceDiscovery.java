package com.tinnkm.rpc.registry;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.jboss.netty.util.internal.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by tinnkm on 2017/11/14.
 * 用于client发现server节点变化,实现负载均衡
 */
public class ServiceDiscovery {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDiscovery.class);
    private volatile List<String> dataList = new ArrayList<String>();

    /**
     * 创建连接并监听
     * @param registryAddress
     */
    public ServiceDiscovery(String registryAddress){
        ZooKeeper zk = new ServiceRegistry(registryAddress).connectServer();
        if (zk != null){
            watchNode(zk);
        }
    }

    /**
     * 发现新节点
     * @return
     */
    public String discover(){
        String data = null;
        int size = dataList.size();
        if (size>0){
            if (size == 1){
                data = dataList.get(0);
                LOGGER.debug("using only data;{}",data);
            }else{
                data = dataList.get(ThreadLocalRandom.current().nextInt(size));
                LOGGER.debug("using random data;{}",data);
            }
        }
        return data;
    }
    /**
     * 监听节点变化
     * @param zk
     */
    private void watchNode(final ZooKeeper zk) {
        try {
            List<String> nodeList = zk.getChildren(Constant.ZK_REGISTRY_PATH, new Watcher() {
                public void process(WatchedEvent watchedEvent) {
                    //节点变化
                    if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
                        watchNode(zk);
                    }
                }
            });
            List<String> dataList = new ArrayList<String>();
            //循环获取数据
            for (String node : nodeList){
                byte[] data = zk.getData(Constant.ZK_REGISTRY_PATH + "/" + node, false, null);
                dataList.add(new String(data));
            }
            LOGGER.debug("node data:{} ",dataList);
            //将数据写入到成员变量
            this.dataList = dataList;
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
