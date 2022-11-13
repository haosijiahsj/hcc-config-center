package com.hcc.config.center.client;

import com.hcc.config.center.client.convert.Convertions;
import com.hcc.config.center.client.convert.ValueConverter;
import com.hcc.config.center.client.entity.AppConfigInfo;

/**
 * 配置服务，使用此方式直接获取配置，动态配置仍需推送后才能获取到变化值
 *
 * @author hushengjun
 * @date 2022/10/21
 */
public interface ConfigService {

    /**
     * 获取指定key的配置值
     * @param key
     * @return
     */
    default String getConfigValue(String key) {
        AppConfigInfo configInfo = this.getConfigInfo(key);
        return configInfo == null ? null : configInfo.getValue();
    }

    /**
     * 获取指定key的配置值并转换为目标类型，使用内置转换器转换
     * @param key
     * @param targetClass
     * @param <T>
     * @return
     */
    default <T> T getConfigValue(String key, Class<T> targetClass) {
        return Convertions.convertValueToTargetObject(this.getConfigValue(key), targetClass, null);
    }

    /**
     * 获取指定key的配置值并使用给定转换器转换为目标类型
     * @param key
     * @param targetClass
     * @param <T>
     * @return
     */
    default <T> T getConfigValue(String key, Class<T> targetClass, ValueConverter<T> valueConverter) {
        return Convertions.convertValueToTargetObject(this.getConfigValue(key), targetClass, valueConverter);
    }

    /**
     * 获取指定key的配置信息
     * @param key
     * @return
     */
    AppConfigInfo getConfigInfo(String key);

}
