package com.hcc.config.center.client;

import com.hcc.config.center.client.context.ConfigContext;
import com.hcc.config.center.client.entity.AppConfigInfo;

/**
 * 默认的ConfigService实现，从configContext缓存中获取配置
 *
 * @author hushengjun
 * @date 2022/11/12
 */
public class DefaultConfigServiceImpl implements ConfigService {

    private final ConfigContext configContext;

    public DefaultConfigServiceImpl(ConfigContext configContext) {
        this.configContext = configContext;
    }

    /**
     * 获取配置信息
     * @param key
     * @return
     */
    public AppConfigInfo getConfigInfo(String key) {
        return configContext.getConfigInfo(key);
    }

}
