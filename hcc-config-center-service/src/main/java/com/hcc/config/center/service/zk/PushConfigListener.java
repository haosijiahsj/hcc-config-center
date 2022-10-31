package com.hcc.config.center.service.zk;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.hcc.config.center.domain.constants.NodePathConstants;
import com.hcc.config.center.domain.enums.AppModeEnum;
import com.hcc.config.center.domain.vo.PushConfigClientMsgVo;
import com.hcc.config.center.domain.vo.PushConfigNodeDataVo;
import com.hcc.config.center.service.longpolling.LongPollingContext;
import com.hcc.config.center.service.netty.NettyChannelContext;
import com.hcc.config.center.service.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

/**
 * PushConfigListener
 *
 * @author shengjun.hu
 * @date 2022/10/19
 */
@Slf4j
@Component
public class PushConfigListener implements ApplicationListener<ApplicationReadyEvent>, DisposableBean {

    @Autowired
    private CuratorFramework curatorFramework;

    private TreeCache treeCache;

    @PostConstruct
    private void init() {
        this.treeCache = new TreeCache(curatorFramework, NodePathConstants.PUSH_CONFIG_PATH);
    }

    private void startListener() {
        treeCache.getListenable()
                .addListener((client, event) -> {
                    TreeCacheEvent.Type type = event.getType();
                    if (event.getData() == null) {
                        return;
                    }
                    String path = event.getData().getPath();
                    if (path.equals(NodePathConstants.PUSH_CONFIG_PATH)) {
                        log.info("节点{}变化，变化类型：{}，忽略", path, type);
                        return;
                    }

                    if (TreeCacheEvent.Type.NODE_ADDED.equals(type)
                            || TreeCacheEvent.Type.NODE_UPDATED.equals(type)) {
                        String jsonData = new String(event.getData().getData(), StandardCharsets.UTF_8);
                        log.info("节点{}变化，变化类型：{}，值：{}", path, type, jsonData);
                        this.pushToClient(path, jsonData);
                    } else {
                        log.info("节点{}变化，变化类型：{}，忽略", path, type);
                    }
                });
        try {
            treeCache.start();
        } catch (Exception e) {
            throw new IllegalStateException("启动节点监听失败！", e);
        }
    }

    private void pushToClient(String path, String jsonData) {
        if (StrUtil.isEmpty(jsonData)) {
            log.info("节点：{}值为空，忽略！", path);
            return;
        }

        PushConfigNodeDataVo nodeDataVo = JsonUtils.toObject(jsonData, PushConfigNodeDataVo.class);

        boolean exist = false;
        if (AppModeEnum.LONG_CONNECT.name().equals(nodeDataVo.getAppMode())) {
            exist = NettyChannelContext.existAppCodeClient(nodeDataVo.getAppCode());
        } else if (AppModeEnum.LONG_POLLING.name().equals(nodeDataVo.getAppMode())) {
            exist = LongPollingContext.existAppCodeClient(nodeDataVo.getAppCode());
        }
        if (!exist) {
            log.info("当前实例不存在appCode: [{}]的客户端连接，忽略！", nodeDataVo.getAppCode());
            return;
        }

        if (CollUtil.isNotEmpty(nodeDataVo.getClientIds())) {
            for (String clientId : nodeDataVo.getClientIds()) {
                PushConfigClientMsgVo msgVo = new PushConfigClientMsgVo();
                BeanUtils.copyProperties(nodeDataVo, msgVo);
                msgVo.setClientId(clientId);

                NettyChannelContext.sendMsg(clientId, msgVo);
            }
        } else {
            PushConfigClientMsgVo msgVo = new PushConfigClientMsgVo();
            BeanUtils.copyProperties(nodeDataVo, msgVo);

            if (AppModeEnum.LONG_CONNECT.name().equals(nodeDataVo.getAppMode())) {
                NettyChannelContext.sendMsgToApp(nodeDataVo.getAppCode(), msgVo);
            } else if (AppModeEnum.LONG_POLLING.name().equals(nodeDataVo.getAppMode())) {
                LongPollingContext.publish(nodeDataVo.getAppCode(), msgVo);
            } else {
                throw new IllegalArgumentException(String.format("未知的模式：%s", nodeDataVo.getAppMode()));
            }
        }
    }

//    private void startListener() {
//        // 只能监听一级
//        PathChildrenCache pathChildrenCache = new PathChildrenCache(curatorFramework, NodePathConstants.PUSH_CONFIG_PATH, true);
//        pathChildrenCache.getListenable()
//                .addListener((client, event) -> {
//                    PathChildrenCacheEvent.Type eventType = event.getType();
//                    if (PathChildrenCacheEvent.Type.CHILD_ADDED.equals(eventType)
//                            || PathChildrenCacheEvent.Type.CHILD_UPDATED.equals(eventType)
//                            || PathChildrenCacheEvent.Type.CHILD_REMOVED.equals(eventType)) {
//                        byte[] data = event.getData().getData();
//                        String jsonData = new String(data, StandardCharsets.UTF_8);
//
//                        PushConfigNodeDataVo nodeDataVo = JsonUtils.toObject(jsonData, PushConfigNodeDataVo.class);
//                        PushConfigClientMsgVo msgVo = new PushConfigClientMsgVo();
//                        BeanUtils.copyProperties(nodeDataVo, msgVo);
//                        if (PathChildrenCacheEvent.Type.CHILD_ADDED.equals(eventType)) {
//                            msgVo.setMsgType(PushConfigMsgType.CONFIG_CREATE.name());
//                            log.info("节点{}创建，值：{}", event.getData().getPath(), jsonData);
//                        } else if (PathChildrenCacheEvent.Type.CHILD_UPDATED.equals(eventType)) {
//                            msgVo.setMsgType(PushConfigMsgType.CONFIG_UPDATE.name());
//                            log.info("节点{}变化，值：{}", event.getData().getPath(), jsonData);
//                        } else {
//                            msgVo.setMsgType(PushConfigMsgType.CONFIG_DELETE.name());
//                            log.info("节点{}删除，值：{}", event.getData().getPath(), jsonData);
//                        }
//
//                        NettyChannelManage.sendMsgToApp(nodeDataVo.getAppCode(), msgVo);
//                    }
//                });
//        try {
//            pathChildrenCache.start();
//        } catch (Exception e) {
//            throw new IllegalStateException("启动节点监听失败！", e);
//        }
//    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        this.startListener();
        log.info("zk path: {}启动监听成功", NodePathConstants.PUSH_CONFIG_PATH);
    }

    @Override
    public void destroy() throws Exception {
        if (treeCache != null) {
            treeCache.close();
            log.info("zk path: {}监听关闭", NodePathConstants.PUSH_CONFIG_PATH);
        }
    }
}
