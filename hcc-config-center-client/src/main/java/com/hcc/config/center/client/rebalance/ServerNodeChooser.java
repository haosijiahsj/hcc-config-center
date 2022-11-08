package com.hcc.config.center.client.rebalance;

import com.hcc.config.center.client.context.ConfigContext;
import com.hcc.config.center.client.entity.ServerNodeInfo;

/**
 * 服务节点选择器
 *
 * @author hushengjun
 * @date 2022/10/22
 */
@FunctionalInterface
public interface ServerNodeChooser {

    /**
     * 选择服务节点算法
     * @param configContext
     * @return
     */
    ServerNodeInfo chooseServerNode(ConfigContext configContext);

}
