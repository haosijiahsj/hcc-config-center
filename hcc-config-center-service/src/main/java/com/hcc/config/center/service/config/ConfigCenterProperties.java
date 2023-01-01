package com.hcc.config.center.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * ConfigCenterConfig
 *
 * @author shengjun.hu
 * @date 2022/10/12
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "config.center")
public class ConfigCenterProperties {

    private String zkAddress;
    private Integer zkSessionTimeOut;
    private String zkNamespace;

    private Integer serverPort;
    private Integer bossThreads;
    private Integer workerThreads;

}
