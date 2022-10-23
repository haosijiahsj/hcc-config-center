package com.hcc.config.center.client.connect;

import com.hcc.config.center.client.context.ConfigContext;
import com.hcc.config.center.client.entity.ServerNodeInfo;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * DefaultServerNodeChooser
 *
 * @author hushengjun
 * @date 2022/10/22
 */
public class DefaultServerNodeChooser implements ServerNodeChooser {

    @Override
    public ServerNodeInfo chooseServerNode(ConfigContext configContext) {
        List<ServerNodeInfo> serverNodeInfos = configContext.getServerNodeInfos();
        if (CollectionUtils.isEmpty(serverNodeInfos)) {
            return null;
        }
        if (serverNodeInfos.size() == 1) {
            return serverNodeInfos.get(0);
        }

        int size = serverNodeInfos.size();
        int index = configContext.getAppCode().hashCode() % size;
        return serverNodeInfos.get(index);
    }

}
