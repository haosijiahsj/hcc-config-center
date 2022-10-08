package com.hcc.config.center.client.spring;

import com.hcc.config.center.client.annotation.RefreshValue;
import com.hcc.config.center.client.annotation.StaticValue;
import com.hcc.config.center.client.context.ConfigCenterContext;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeansException;
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
@NoArgsConstructor
@AllArgsConstructor
public class ConfigCenterBeanPostProcessor implements BeanPostProcessor {

    private ConfigCenterContext configCenterContext;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Field[] declaredFields = bean.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            StaticValue staticValue = AnnotationUtils.findAnnotation(field.getType(), StaticValue.class);
            RefreshValue refreshValue = AnnotationUtils.findAnnotation(field.getType(), RefreshValue.class);
            if (staticValue == null && refreshValue == null) {
                continue;
            }
            String configKey = staticValue != null ? staticValue.value() : refreshValue.value();
            this.injectConfigValue(configKey, bean, field);
            if (refreshValue != null) {
                this.collectRefreshValue(configKey, beanName, field);
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
            field.set(bean, this.convertValueToTargetType(configValue, field.getType()));
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
    private void collectRefreshValue(String configKey, String beanName, Field field) {}

    /**
     * 转换为目标类型
     * @param value
     * @param targetClass
     * @return
     */
    private Object convertValueToTargetType(String value, Class<?> targetClass) {
        if (String.class.equals(targetClass)) {
            return value;
        }

        Object targetValue;
        if (Byte.class.equals(targetClass)) {
            targetValue = Byte.valueOf(value);
        } else if (Short.class.equals(targetClass)) {
            targetValue = Short.valueOf(value);
        } else if (Integer.class.equals(targetClass)) {
            targetValue = Integer.valueOf(value);
        } else if (Long.class.equals(targetClass)) {
            targetValue = Long.valueOf(value);
        } else if (Float.class.equals(targetClass)) {
            targetValue = Float.valueOf(value);
        } else if (Double.class.equals(targetClass)) {
            targetValue = Double.valueOf(value);
        } else if (Character.class.equals(targetClass)) {
            if (value.length() > 1) {
                throw new IllegalArgumentException("不能转换为char");
            }
            targetValue = value.charAt(0);
        } else if (Boolean.class.equals(targetClass)) {
            targetValue = Boolean.valueOf(value);
        } else {
            throw new IllegalArgumentException("不支持的类型");
        }

        return targetValue;
    }

}
