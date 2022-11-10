package com.hcc.config.center.client.spring;

import com.hcc.config.center.client.ConfigChangeHandler;
import com.hcc.config.center.client.ProcessRefreshConfigCallBack;
import com.hcc.config.center.client.context.ConfigContext;
import com.hcc.config.center.client.entity.AppMode;
import com.hcc.config.center.client.entity.ConfigChangeEvent;
import com.hcc.config.center.client.entity.MsgEventType;
import com.hcc.config.center.client.entity.ServerNodeInfo;
import com.hcc.config.center.client.longpolling.ConfigCenterClientLongPolling;
import com.hcc.config.center.client.netty.ConfigCenterClient;
import com.hcc.config.center.client.rebalance.DefaultServerNodeChooser;
import com.hcc.config.center.client.rebalance.ServerNodeChooser;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 客户端初始化器
 *
 * @author shengjun.hu
 * @date 2022/10/13
 */
@Slf4j
public class ConfigCenterClientInitializer {

    private final ConfigContext configContext;
    private final ProcessRefreshConfigCallBack callBack;
    private ServerNodeChooser serverNodeChooser;

    private ConfigCenterClient configCenterClient;

    public ConfigCenterClientInitializer(ConfigContext configContext, ProcessRefreshConfigCallBack callBack, List<ConfigChangeHandler> configChangeHandlers, ServerNodeChooser serverNodeChooser) {
        this.configContext = configContext;
        this.callBack = callBack == null ? new ProcessRefreshConfigCallBack() {} : callBack;
        this.serverNodeChooser = serverNodeChooser;
        if (AppMode.LONG_CONNECT.name().equals(configContext.getAppMode()) && this.serverNodeChooser == null) {
            this.serverNodeChooser = new DefaultServerNodeChooser();
        }
        this.invokeConfigChangeHandler(configChangeHandlers);
    }

    /**
     * 初始化调用handler
     */
    private void invokeConfigChangeHandler(List<ConfigChangeHandler> configChangeHandlers) {
        if (configChangeHandlers == null || configChangeHandlers.isEmpty()) {
            return;
        }
        for (ConfigChangeHandler handler : configChangeHandlers) {
            // 添加到上下文
            configContext.addConfigChangeHandler(handler);
            // 第一次执行handler
            configContext.getConfigKeyValueMap()
                    .entrySet()
                    .stream()
                    .filter(entry -> handler.keys() != null && handler.keys().contains(entry.getKey()))
                    .forEach(entry -> {
                        ConfigChangeEvent event = new ConfigChangeEvent();
                        event.setKey(entry.getKey());
                        event.setEventType(MsgEventType.CONFIG_INIT);
                        event.setNewValue(entry.getValue());
                        handler.onChange(event);
                    });
        }
    }

    /**
     * 根据不同的模式启动客户端
     */
    public void startClient() {
        if (!configContext.isEnableDynamicPush()) {
            log.warn("未开启动态推送开关，不启动配置中心客户端");
            return;
        }

        if (AppMode.LONG_CONNECT.name().equals(configContext.getAppMode())) {
            ServerNodeInfo serverNode = serverNodeChooser.chooseServerNode(configContext);
            if (serverNode == null) {
                throw new IllegalStateException("未获取到配置中心服务节点！");
            }

            configCenterClient = new ConfigCenterClient(serverNode.getHost(), serverNode.getPort(),
                    configContext, callBack, serverNodeChooser);

            new Thread(configCenterClient::startUp).start();
        } else if (AppMode.LONG_POLLING.name().equals(configContext.getAppMode())) {
            ConfigCenterClientLongPolling longPolling = new ConfigCenterClientLongPolling(configContext, callBack);

            longPolling.startUp();
        } else {
            throw new IllegalStateException(String.format("启动异常，不支持的模式：[%s]", configContext.getAppMode()));
        }
    }

    /**
     * 关闭客户端
     */
    public void stopClient() {
        if (configCenterClient != null) {
            configCenterClient.stop();
        }
    }

}
