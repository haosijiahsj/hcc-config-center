package com.hcc.config.center.service;

/**
 * ApplicationConfigPushService
 *
 * @author shengjun.hu
 * @date 2022/10/19
 */
public interface ApplicationConfigPushService {

    /**
     * 推送配置
     * @param id
     * @param forceUpdate
     */
    void pushConfig(Long id, Boolean forceUpdate);

    /**
     * 推送已删除的配置
     * @param id
     */
    void pushDeletedConfig(Long id);

}
