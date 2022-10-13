package com.hcc.config.center.client.spring;

import com.hcc.config.center.client.constant.Constants;
import com.hcc.config.center.client.context.ConfigCenterContext;
import com.hcc.config.center.client.entity.ServerNodeInfo;
import com.hcc.config.center.client.netty.ConfigCenterClient;
import com.hcc.config.center.client.utils.RestTemplateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ConfigCenterListener
 *
 * @author shengjun.hu
 * @date 2022/10/13
 */
public class ConfigCenterListener implements ApplicationListener<ContextStartedEvent> {

    @Autowired
    private ConfigCenterContext configCenterContext;

    @Override
    public void onApplicationEvent(ContextStartedEvent contextStartedEvent) {
        if (!configCenterContext.isEnableDynamicPush()) {
            return;
        }

        ConfigCenterClient configCenterClient = new ConfigCenterClient();
        configCenterClient.setConfigCenterContext(configCenterContext);

        ServerNodeInfo serverNode = this.findServerNode(configCenterContext);
        if (serverNode == null) {
            return;
        }
        configCenterClient.setHost(serverNode.getHost());
        configCenterClient.setPort(serverNode.getPort());

        configCenterClient.startUp();
    }

    /**
     * 负载节点
     * @param configCenterContext
     * @return
     */
    private ServerNodeInfo findServerNode(ConfigCenterContext configCenterContext) {
        String serverNodeUrl = configCenterContext.getConfigCenterUrl() + Constants.SERVER_NODE_URI;

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("appCode", configCenterContext.getAppCode());
        paramMap.put("secretKey", configCenterContext.getSecretKey());
        List<ServerNodeInfo> serverNodeInfos = RestTemplateUtils.getList(serverNodeUrl, paramMap);
        if (CollectionUtils.isEmpty(serverNodeInfos)) {
            return null;
        }

        int nodeSize = serverNodeInfos.size();
        // 分配服务器
        if (nodeSize == 1) {
            return serverNodeInfos.get(0);
        }

        // TODO 同一appCode注册到一台机器上
        return null;
    }

}
