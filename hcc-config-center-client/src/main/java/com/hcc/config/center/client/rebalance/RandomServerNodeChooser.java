package com.hcc.config.center.client.rebalance;

import com.hcc.config.center.client.context.ConfigContext;
import com.hcc.config.center.client.entity.ServerNodeInfo;
import com.hcc.config.center.client.utils.CollUtils;

import java.util.List;
import java.util.Random;

/**
 * 随机的服务选择器
 *
 * @author shengjun.hu
 * @date 2022/11/2
 */
public class RandomServerNodeChooser implements ServerNodeChooser {

    @Override
    public ServerNodeInfo chooseServerNode(ConfigContext configContext) {
        List<ServerNodeInfo> serverNodeInfos = configContext.getServerNodeInfos();
        if (CollUtils.isEmpty(serverNodeInfos)) {
            return null;
        }
        if (serverNodeInfos.size() == 1) {
            return serverNodeInfos.get(0);
        }

        int i = new Random().nextInt(serverNodeInfos.size());

        return serverNodeInfos.get(i);
    }

}
