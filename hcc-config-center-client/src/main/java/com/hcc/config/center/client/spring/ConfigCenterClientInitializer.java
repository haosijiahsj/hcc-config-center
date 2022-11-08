package com.hcc.config.center.client.spring;

import com.hcc.config.center.client.ProcessDynamicConfigCallBack;
import com.hcc.config.center.client.rebalance.DefaultServerNodeChooser;
import com.hcc.config.center.client.rebalance.ServerNodeChooser;
import com.hcc.config.center.client.context.ConfigContext;
import com.hcc.config.center.client.entity.AppMode;
import com.hcc.config.center.client.entity.ServerNodeInfo;
import com.hcc.config.center.client.longpolling.ConfigCenterClientLongPolling;
import com.hcc.config.center.client.netty.ConfigCenterClient;
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
    private ProcessDynamicConfigCallBack callBack;
    private ServerNodeChooser serverNodeChooser;

    private ConfigCenterClient configCenterClient;

    public ConfigCenterClientInitializer(ConfigContext configContext, ProcessDynamicConfigCallBack callBack) {
        this(configContext, callBack, null);
    }

    public ConfigCenterClientInitializer(ConfigContext configContext, ProcessDynamicConfigCallBack callBack, ServerNodeChooser serverNodeChooser) {
        this.configContext = configContext;
        this.callBack = callBack;
        if (this.callBack == null) {
            this.callBack = new ProcessDynamicConfigCallBack() {};
        }
        this.serverNodeChooser = serverNodeChooser;
        if (AppMode.LONG_CONNECT.name().equals(configContext.getAppMode()) && this.serverNodeChooser == null) {
            this.serverNodeChooser = new DefaultServerNodeChooser();
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
            ConfigCenterClientLongPolling schedule = new ConfigCenterClientLongPolling(configContext, callBack);

            schedule.startUp();
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
