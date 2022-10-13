package com.hcc.config.center.client.spring;

import com.hcc.config.center.client.annotation.DynamicValue;
import com.hcc.config.center.client.annotation.StaticValue;
import com.hcc.config.center.client.context.ConfigCenterContext;
import com.hcc.config.center.client.entity.DynamicFieldInfo;
import com.hcc.config.center.client.utils.ConvertUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * ConfigCenterBeanPostProcessor
 *
 * @author shengjun.hu
 * @date 2022/10/8
 */
public class ConfigCenterBeanPostProcessor implements BeanPostProcessor {

    @Autowired
    private ConfigCenterContext configCenterContext;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Field[] declaredFields = bean.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            StaticValue staticValue = AnnotationUtils.findAnnotation(field.getType(), StaticValue.class);
            DynamicValue dynamicValue = AnnotationUtils.findAnnotation(field.getType(), DynamicValue.class);
            if (staticValue == null && dynamicValue == null) {
                continue;
            }
            String configKey = staticValue != null ? staticValue.value() : dynamicValue.value();
            this.injectConfigValue(configKey, bean, field);
            if (dynamicValue != null) {
                this.collectDynamicFieldInfo(configKey, beanName, field, bean);
            }
        }

        return bean;
    }

    /**
     * 注入值
     * @param configKey
     * @param bean
     * @param field
     */
    private void injectConfigValue(String configKey, Object bean, Field field) {
        Value value = AnnotationUtils.findAnnotation(field.getType(), Value.class);
        if (value != null) {
            // 使用spring的value注解后，不进行注入，由spring进行注入
            return;
        }
        Map<String, String> configKeyValueMap = configCenterContext.getConfigKeyValueMap();
        String configValue = configKeyValueMap.get(configKey);
        if (configValue == null) {
            return;
        }
        try {
            field.setAccessible(true);
            field.set(bean, ConvertUtils.convertValueToTargetType(configValue, field.getType()));
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 收集需要动态刷新的字段
     * @param configKey
     * @param beanName
     * @param field
     */
    private void collectDynamicFieldInfo(String configKey, String beanName, Field field, Object bean) {
        DynamicFieldInfo dynamicFieldInfo = new DynamicFieldInfo();
        dynamicFieldInfo.setKey(configKey);
        dynamicFieldInfo.setField(field);
        dynamicFieldInfo.setBeanName(beanName);
        dynamicFieldInfo.setBean(bean);
        dynamicFieldInfo.setBeanClass(bean.getClass());

        configCenterContext.addDynamicFieldInfo(dynamicFieldInfo);
    }

}
