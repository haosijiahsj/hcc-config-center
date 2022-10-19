package com.hcc.config.center.service.zk;

import com.hcc.config.center.domain.constants.NodePathConstants;
import com.hcc.config.center.domain.vo.PushConfigNodeDataVo;
import com.hcc.config.center.domain.vo.ServerNodeVo;
import com.hcc.config.center.service.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ZkHandler
 *
 * @author shengjun.hu
 * @date 2022/10/19
 */
@Slf4j
@Component
public class ZkHandler {

    @Autowired
    private CuratorFramework curatorFramework;

    /**
     * 添加推送节点
     * @param nodeData
     */
    public synchronized void addPushConfigNode(PushConfigNodeDataVo nodeData) {
        String path = NodePathConstants.PUSH_CONFIG_PATH + "/" + nodeData.getAppCode() + "_" + nodeData.getKey();
        String data = JsonUtils.toJson(nodeData);
        try {
            Stat stat = curatorFramework.checkExists().forPath(path);
            if (stat == null) {
                curatorFramework.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL)
                        .forPath(path, data.getBytes());
            } else {
                curatorFramework.setData()
                        .withVersion(nodeData.getVersion())
                        .forPath(path, data.getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            log.error("创建推送节点失败！", e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * 注册服务节点
     * @param host
     * @param port
     */
    public synchronized void registerServerNode(String host, Integer port) {
        try {
            curatorFramework.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(NodePathConstants.SERVER_NODE_PATH + String.format("/%s:%s", host, port));
        } catch (Exception e) {
            log.error("创建服务节点失败！", e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * 查询服务节点
     * @return
     */
    public List<ServerNodeVo> findAllServerNode() {
        try {
            List<String> nodePaths = curatorFramework.getChildren().forPath(NodePathConstants.SERVER_NODE_PATH);

            return nodePaths.stream()
                    .map(nodePath -> {
                        String[] split = nodePath.split(":");
                        ServerNodeVo serverNodeVo = new ServerNodeVo();
                        serverNodeVo.setHost(split[0]);
                        serverNodeVo.setPort(Integer.valueOf(split[1]));

                        return serverNodeVo;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取服务节点失败！", e);
            throw new IllegalStateException(e);
        }
    }

}
