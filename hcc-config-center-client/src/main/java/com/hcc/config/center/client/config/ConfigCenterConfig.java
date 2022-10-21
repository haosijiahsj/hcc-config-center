package com.hcc.config.center.client.config;

import com.hcc.config.center.client.context.ConfigCenterContext;
import com.hcc.config.center.client.spring.ConfigCenterBeanPostProcessor;
import com.hcc.config.center.client.spring.ConfigCenterListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * ConfigCenterConfig
 *
 * @author shengjun.hu
 * @date 2022/10/13
 */
@Configuration
public class ConfigCenterConfig {

    @Bean(initMethod = "initContext")
    public ConfigCenterContext configCenterContext(Environment environment) {
        ConfigCenterContext configCenterContext = new ConfigCenterContext();
        String appCode = environment.getRequiredProperty("config.center.appCode");
        String secretKey = environment.getRequiredProperty("config.center.secretKey");
        String serverUrl = environment.getRequiredProperty("config.center.serverUrl");
        Integer serverPort = environment.getProperty("config.center.serverPort", Integer.class);
        Boolean enableDynamicPush = environment.getProperty("config.center.enableDynamicPush", Boolean.class);

        configCenterContext.setAppCode(appCode);
        configCenterContext.setSecretKey(secretKey);
        configCenterContext.setServerUrl(serverUrl);
        if (serverPort != null) {
            configCenterContext.setServerPort(serverPort);
        }
        if (enableDynamicPush != null) {
            configCenterContext.setEnableDynamicPush(enableDynamicPush);
        }

        return configCenterContext;
    }

    @Bean
    public ConfigCenterBeanPostProcessor configCenterBeanPostProcessor() {
        return new ConfigCenterBeanPostProcessor();
    }

    @Bean(initMethod = "start")
    public ConfigCenterListener configCenterListener() {
        return new ConfigCenterListener();
    }

}
