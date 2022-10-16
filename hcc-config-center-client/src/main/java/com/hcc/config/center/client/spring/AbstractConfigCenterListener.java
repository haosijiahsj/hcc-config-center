package com.hcc.config.center.client.spring;

import com.hcc.config.center.client.constant.Constants;
import com.hcc.config.center.client.context.ConfigCenterContext;
import com.hcc.config.center.client.entity.ServerNodeInfo;
import com.hcc.config.center.client.netty.ConfigCenterClient;
import com.hcc.config.center.client.utils.RestTemplateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
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
@Slf4j
public abstract class AbstractConfigCenterListener<T extends ApplicationEvent> implements ApplicationListener<T> {

    @Autowired
    private ConfigCenterContext configCenterContext;

    @Override
    public void onApplicationEvent(T contextStartedEvent) {
        if (!configCenterContext.isEnableDynamicPush()) {
            log.info("未开启动态推送开关，无法启动配置中心客户端");
            return;
        }

        ServerNodeInfo serverNode = this.findServerNode(configCenterContext);
        if (serverNode == null) {
            log.warn("没有获取到配置中心服务节点，无法启动配置中心客户端");
            return;
        }
        ConfigCenterClient configCenterClient = new ConfigCenterClient();
        configCenterClient.setConfigCenterContext(configCenterContext);
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

        return this.chooseServerNode(configCenterContext, serverNodeInfos);
    }

    /**
     * 负责均衡算法
     * @return
     */
    protected ServerNodeInfo chooseServerNode(ConfigCenterContext configCenterContext, List<ServerNodeInfo> serverNodeInfos) {
        return null;
    }

}
