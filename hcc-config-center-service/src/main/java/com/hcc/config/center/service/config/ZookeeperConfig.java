package com.hcc.config.center.service.config;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ZookeeperConfig
 *
 * @author shengjun.hu
 * @date 2022/10/11
 */
@Configuration
public class ZookeeperConfig {

    @Autowired
    private ConfigCenterProperties configCenterProperties;

    @Bean(initMethod = "start", destroyMethod = "close")
    public CuratorFramework curatorFramework() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(configCenterProperties.getZkAddress())
                .sessionTimeoutMs(configCenterProperties.getZkSessionTimeOut())
                .retryPolicy(retryPolicy)
                .namespace(configCenterProperties.getZkNamespace());

        return builder.build();
    }

}
