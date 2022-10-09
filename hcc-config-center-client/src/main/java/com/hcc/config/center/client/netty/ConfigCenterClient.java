package com.hcc.config.center.client.netty;

import com.hcc.config.center.client.context.ConfigCenterContext;
import io.netty.bootstrap.Bootstrap;

/**
 * 客户端，接受服务端消息，动态刷新值
 *
 * @author shengjun.hu
 * @date 2022/10/9
 */
public class ConfigCenterClient {

    private ConfigCenterContext configCenterContext;
    private Bootstrap clientBootStrap;

    /**
     * 启动客户端
     */
    public void startUp() {
    }

}
