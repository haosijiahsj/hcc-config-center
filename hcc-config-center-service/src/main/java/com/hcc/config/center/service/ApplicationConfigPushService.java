package com.hcc.config.center.service;

/**
 * ApplicationConfigPushService
 *
 * @author shengjun.hu
 * @date 2022/10/19
 */
public interface ApplicationConfigPushService {

    void pushConfig(Long id, Boolean forceUpdate);

    void pushDeleteConfig(String appCode, String key);

}
