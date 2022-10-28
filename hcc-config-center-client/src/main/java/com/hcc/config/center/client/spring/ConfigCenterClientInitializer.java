package com.hcc.config.center.client.spring;

import com.hcc.config.center.client.ProcessFailedCallBack;
import com.hcc.config.center.client.connect.DefaultServerNodeChooser;
import com.hcc.config.center.client.connect.ServerNodeChooser;
import com.hcc.config.center.client.context.ConfigContext;
import com.hcc.config.center.client.entity.AppMode;
import com.hcc.config.center.client.entity.ServerNodeInfo;
import com.hcc.config.center.client.netty.ConfigCenterClient;
import com.hcc.config.center.client.longpolling.ConfigCenterClientLongPolling;
import lombok.extern.slf4j.Slf4j;

/**
 * 客户端初始化器
 *
 * @author shengjun.hu
 * @date 2022/10/13
 */
@Slf4j
public class ConfigCenterClientInitializer {

    private final ConfigContext configContext;
    private final ProcessFailedCallBack callBack;

    private final ServerNodeChooser serverNodeChooser;

    public ConfigCenterClientInitializer(ConfigContext configContext, ProcessFailedCallBack callBack) {
        this.configContext = configContext;
        this.callBack = callBack;
        this.serverNodeChooser = new DefaultServerNodeChooser();
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

            ConfigCenterClient configCenterClient = new ConfigCenterClient();
            configCenterClient.setCallBack(callBack);
            configCenterClient.setConfigContext(configContext);
            configCenterClient.setHost(serverNode.getHost());
            configCenterClient.setPort(serverNode.getPort());

            new Thread(configCenterClient::startUp).start();
        } else if (AppMode.LONG_POLLING.name().equals(configContext.getAppMode())) {
            ConfigCenterClientLongPolling schedule = new ConfigCenterClientLongPolling(configContext, callBack);

            schedule.startUp();
        } else {
            throw new IllegalStateException(String.format("启动异常，不支持的模式：[%s]", configContext.getAppMode()));
        }
    }

}
