package com.hcc.config.center.client;

import com.hcc.config.center.client.context.ConfigContext;
import com.hcc.config.center.client.entity.AppConfigInfo;
import com.hcc.config.center.client.convert.Convertions;

/**
 * 配置服务，使用此方式直接获取配置，动态配置仍需推送后才能获取到变化值
 *
 * @author hushengjun
 * @date 2022/10/21
 */
public class ConfigService {

    private final ConfigContext configContext;

    public ConfigService(ConfigContext configContext) {
        this.configContext = configContext;
    }

    /**
     * 获取指定key的版本
     * @param key
     * @return
     */
    public Integer getConfigVersion(String key) {
        return configContext.getConfigVersion(key);
    }

    /**
     * 获取配置值
     * @param key
     * @return
     */
    public String getConfigValue(String key) {
        return configContext.getConfigValue(key);
    }

    /**
     * 获取配置值并转换为目标类型
     * @param key
     * @param targetClass
     * @param <T>
     * @return
     */
    public <T> T getConfigValue(String key, Class<T> targetClass) {
        return Convertions.convertValueToTargetObject(this.getConfigValue(key), targetClass);
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
