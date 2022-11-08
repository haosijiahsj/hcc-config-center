package com.hcc.config.center.client.config;

import com.hcc.config.center.client.ConfigService;
import com.hcc.config.center.client.ProcessDynamicConfigCallBack;
import com.hcc.config.center.client.rebalance.ServerNodeChooser;
import com.hcc.config.center.client.context.ConfigContext;
import com.hcc.config.center.client.entity.AppMode;
import com.hcc.config.center.client.spring.ConfigCenterBeanPostProcessor;
import com.hcc.config.center.client.spring.ConfigCenterClientInitializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

/**
 * 配置类，使用@Import注解生效
 *
 * @author shengjun.hu
 * @date 2022/10/13
 */
@Configuration
public class ConfigCenterConfig {

    private final ConfigContext configContext;

    public ConfigCenterConfig(Environment environment) {
        this.configContext = this.buildConfigContext(environment);
    }

    /**
     * 构建ConfigContext
     * @param environment
     * @return
     */
    public ConfigContext buildConfigContext(Environment environment) {
        ConfigContext configContext = new ConfigContext();
        String appCode = environment.getRequiredProperty("config.center.appCode");
        String secretKey = environment.getRequiredProperty("config.center.secretKey");
        String serverUrl = environment.getRequiredProperty("config.center.serverUrl");
        Boolean enableDynamicPush = environment.getProperty("config.center.enableDynamicPush", Boolean.class);
        Boolean checkConfigExist = environment.getProperty("config.center.checkConfigExist", Boolean.class);
        Integer pullInterval = environment.getProperty("config.center.pullInterval", Integer.class);
        Integer longPollingTimeout = environment.getProperty("config.center.longPollingTimeout", Integer.class);

        configContext.setAppCode(appCode);
        configContext.setSecretKey(secretKey);
        configContext.setServerUrl(serverUrl);
        if (enableDynamicPush != null) {
            configContext.setEnableDynamicPush(enableDynamicPush);
        }
        if (checkConfigExist != null) {
            configContext.setCheckConfigExist(checkConfigExist);
        }
        if (pullInterval != null) {
            configContext.setPullInterval(pullInterval);
        }
        if (longPollingTimeout != null) {
            configContext.setLongPollingTimeout(longPollingTimeout);
        }
        configContext.initContext();

        if (AppMode.LONG_POLLING.name().equals(configContext.getAppMode())) {
            Assert.isTrue(configContext.getPullInterval() >= 300, "拉取时间间隔不得小于300s");
            Assert.isTrue(configContext.getLongPollingTimeout() >= 90, "长轮询超时时间不得小于90s");
        }

        return configContext;
    }

    /**
     * 暴露ConfigService bean
     * @return
     */
    @Bean
    public ConfigService configService() {
        return new ConfigService(configContext);
    }

    /**
     * 初始化值注入以及动态字段监听方法收集
     * @return
     */
    @Bean
    public ConfigCenterBeanPostProcessor configCenterBeanPostProcessor() {
        return new ConfigCenterBeanPostProcessor(configContext);
    }

    /**
     * 启动配置中心监听
     * @param callBackObjectProvider
     * @return
     */
    @Bean(destroyMethod = "stopClient")
    public ConfigCenterClientInitializer configCenterClientInitializer(ObjectProvider<ProcessDynamicConfigCallBack> callBackObjectProvider,
                                                                       ObjectProvider<ServerNodeChooser> serverNodeChooserObjectProvider) {
        ProcessDynamicConfigCallBack callBack = callBackObjectProvider.getIfAvailable();
        ServerNodeChooser serverNodeChooser = serverNodeChooserObjectProvider.getIfAvailable();
        ConfigCenterClientInitializer initializer = new ConfigCenterClientInitializer(configContext, callBack, serverNodeChooser);
        initializer.startClient();

        return initializer;
    }

}
