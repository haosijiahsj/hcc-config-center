package com.hcc.config.center.server.config;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
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

    @Value("${__infra.business.zookeeper.addr}")
    private String zkAddress;

    @Bean
    public CuratorFramework curatorFramework() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(zkAddress)
                .sessionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .namespace("hcc-config-center");
        CuratorFramework curatorFramework = builder.build();
        curatorFramework.start();

        return curatorFramework;
    }

}
