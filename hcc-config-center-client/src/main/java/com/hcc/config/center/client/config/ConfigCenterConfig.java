package com.hcc.config.center.client.config;

import com.hcc.config.center.client.context.ConfigCenterContext;
import com.hcc.config.center.client.spring.ConfigCenterBeanPostProcessor;
import com.hcc.config.center.client.spring.ConfigCenterListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ConfigCenterConfig
 *
 * @author shengjun.hu
 * @date 2022/10/13
 */
@Configuration
public class ConfigCenterConfig {

    @Bean
    public ConfigCenterContext configCenterContext() {
        ConfigCenterContext configCenterContext = new ConfigCenterContext();
        configCenterContext.initContext();

        return configCenterContext;
    }

    @Bean
    public ConfigCenterBeanPostProcessor configCenterBeanPostProcessor() {
        return new ConfigCenterBeanPostProcessor();
    }

    @Bean
    public ConfigCenterListener configCenterListener() {
        return new ConfigCenterListener();
    }

}
