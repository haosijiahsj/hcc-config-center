package com.hcc.config.center.client.rebalance;

import com.hcc.config.center.client.context.ConfigContext;
import com.hcc.config.center.client.entity.ServerNodeInfo;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 默认的服务选择器（secretKey hash取模）
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
        int index = configContext.getSecretKey().hashCode() % size;
        return serverNodeInfos.get(index);
    }

}
