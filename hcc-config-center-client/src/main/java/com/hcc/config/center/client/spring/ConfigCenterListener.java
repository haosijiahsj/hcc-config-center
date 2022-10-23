package com.hcc.config.center.client.spring;

import com.hcc.config.center.client.ProcessFailedCallBack;
import com.hcc.config.center.client.connect.DefaultServerNodeChooser;
import com.hcc.config.center.client.connect.ServerNodeChooser;
import com.hcc.config.center.client.context.ConfigContext;
import com.hcc.config.center.client.entity.ServerNodeInfo;
import com.hcc.config.center.client.netty.ConfigCenterClient;
import lombok.extern.slf4j.Slf4j;

/**
 * ConfigCenterListener
 *
 * @author shengjun.hu
 * @date 2022/10/13
 */
@Slf4j
public class ConfigCenterListener {

    private final ConfigContext configContext;
    private final ProcessFailedCallBack callBack;

    private final ServerNodeChooser serverNodeChooser;

    public ConfigCenterListener(ConfigContext configContext, ProcessFailedCallBack callBack) {
        this.configContext = configContext;
        this.callBack = callBack;
        this.serverNodeChooser = new DefaultServerNodeChooser();
    }

    public void start() {
        if (!configContext.isEnableDynamicPush()) {
            log.warn("未开启动态推送开关，不启动配置中心客户端");
            return;
        }

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
    }

}
