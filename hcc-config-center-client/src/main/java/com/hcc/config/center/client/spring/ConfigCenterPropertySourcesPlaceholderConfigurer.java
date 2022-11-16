package com.hcc.config.center.client.spring;

import com.hcc.config.center.client.context.ConfigContext;
import com.hcc.config.center.client.utils.CollUtils;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * 为了spring的Value注解能够获取到值
 *
 * @author hushengjun
 * @date 2022/10/9
 */
public class ConfigCenterPropertySourcesPlaceholderConfigurer extends PropertySourcesPlaceholderConfigurer {

    private final ConfigContext configContext;

    public ConfigCenterPropertySourcesPlaceholderConfigurer(ConfigContext configContext) {
        this.configContext = configContext;
    }

    @Override
    protected void loadProperties(Properties props) throws IOException {
        super.loadProperties(props);
        Map<String, String> configKeyValueMap = configContext.getConfigKeyValueMap();
        if (CollUtils.isNotEmpty(configKeyValueMap)) {
            props.putAll(configKeyValueMap);
        }
    }

}
