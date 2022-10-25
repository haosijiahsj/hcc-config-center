package com.hcc.config.center.client.config;

import com.hcc.config.center.client.ConfigService;
import com.hcc.config.center.client.ProcessFailedCallBack;
import com.hcc.config.center.client.context.ConfigContext;
import com.hcc.config.center.client.spring.ConfigCenterBeanPostProcessor;
import com.hcc.config.center.client.spring.ConfigCenterClientInitializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * 配置类，使用@Import注解生效
 *
 * @author shengjun.hu
 * @date 2022/10/13
 */
@Configuration
public class ConfigCenterConfig {

    /**
     * 暴露ConfigContext bean
     * @param environment
     * @return
     */
    @Bean
    public ConfigContext configContext(Environment environment) {
        ConfigContext configContext = new ConfigContext();
        String appCode = environment.getRequiredProperty("config.center.appCode");
        String secretKey = environment.getRequiredProperty("config.center.secretKey");
        String serverUrl = environment.getRequiredProperty("config.center.serverUrl");
        Boolean enableDynamicPush = environment.getProperty("config.center.enableDynamicPush", Boolean.class);
        Integer pullInterval = environment.getProperty("config.center.pullInterval", Integer.class);
        Integer longPollingTimeout = environment.getProperty("config.center.longPollingTimeout", Integer.class);

        configContext.setAppCode(appCode);
        configContext.setSecretKey(secretKey);
        configContext.setServerUrl(serverUrl);
        if (enableDynamicPush != null) {
            configContext.setEnableDynamicPush(enableDynamicPush);
        }
        if (pullInterval != null) {
            configContext.setPullInterval(pullInterval);
        }
        if (longPollingTimeout != null) {
            configContext.setLongPollingTimeout(longPollingTimeout);
        }
        configContext.initContext();

        return configContext;
    }

    /**
     * 暴露ConfigService bean
     * @param configContext
     * @return
     */
    @Bean
    public ConfigService configService(ConfigContext configContext) {
        return new ConfigService(configContext);
    }

    /**
     * 初始化值注入以及动态字段监听方法收集
     * @param configContext
     * @return
     */
    @Bean
    public ConfigCenterBeanPostProcessor configCenterBeanPostProcessor(ConfigContext configContext) {
        return new ConfigCenterBeanPostProcessor(configContext);
    }

    /**
     * 启动配置中心监听
     * @param configContext
     * @param callBackObjectProvider
     * @return
     */
    @Bean
    public ConfigCenterClientInitializer configCenterClientInitializer(ConfigContext configContext, ObjectProvider<ProcessFailedCallBack> callBackObjectProvider) {
        ProcessFailedCallBack callBack = callBackObjectProvider.getIfUnique(() -> new ProcessFailedCallBack() {});
        ConfigCenterClientInitializer initializer = new ConfigCenterClientInitializer(configContext, callBack);
        initializer.startClient();

        return initializer;
    }

}
