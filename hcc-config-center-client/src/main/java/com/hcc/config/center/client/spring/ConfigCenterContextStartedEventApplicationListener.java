package com.hcc.config.center.client.spring;

import com.hcc.config.center.client.context.ConfigCenterContext;
import com.hcc.config.center.client.entity.ServerNodeInfo;
import org.springframework.context.event.ContextStartedEvent;

import java.util.List;

/**
 * ConfigCenterContextStartedEventApplicationListener
 *
 * @author hushengjun
 * @date 2022/10/14
 */
public class ConfigCenterContextStartedEventApplicationListener extends AbstractConfigCenterListener<ContextStartedEvent> {

    @Override
    protected ServerNodeInfo chooseServerNode(ConfigCenterContext configCenterContext, List<ServerNodeInfo> serverNodeInfos) {
        return null;
    }

}
