package com.hcc.config.center.client.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.util.Properties;

/**
 * StaticValuePropertySourcesPlaceholderConfigurer
 *
 * @author hushengjun
 * @date 2022/10/9
 */
public class StaticValuePropertySourcesPlaceholderConfigurer extends PropertySourcesPlaceholderConfigurer {

    @Override
    public void setEnvironment(Environment environment) {
        super.setEnvironment(environment);
    }

    @Override
    protected Properties mergeProperties() throws IOException {
        return super.mergeProperties();
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        super.postProcessBeanFactory(beanFactory);
    }

}
