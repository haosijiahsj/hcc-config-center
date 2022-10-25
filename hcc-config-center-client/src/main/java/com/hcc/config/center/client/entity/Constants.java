package com.hcc.config.center.client.entity;

/**
 * Constants
 *
 * @author shengjun.hu
 * @date 2022/10/9
 */
public interface Constants {

    /**
     * 配置中心获取配置地址
     */
    String APP_CONFIG_URI = "/config-center/get-app-config";
    String DYNAMIC_APP_CONFIG_URI = "/config-center/get-dynamic-app-config";
    String WATCH_URI = "/config-center/watch";
    /**
     * 配置中心获取服务节点地址
     */
    String SERVER_NODE_URI = "/config-center/get-server-node";
    /**
     * 配置中心获取应用信息地址
     */
    String APP_INFO_URI = "/config-center/get-app";

}
