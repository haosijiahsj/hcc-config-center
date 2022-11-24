package com.hcc.config.center.client.spring;

import com.hcc.config.center.client.annotation.ConfigListener;
import com.hcc.config.center.client.annotation.ConfigValue;
import com.hcc.config.center.client.context.ConfigContext;
import com.hcc.config.center.client.convert.Convertions;
import com.hcc.config.center.client.convert.ValueConverter;
import com.hcc.config.center.client.entity.AppConfigInfo;
import com.hcc.config.center.client.entity.RefreshConfigRefInfo;
import com.hcc.config.center.client.utils.ReflectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * ConfigCenterBeanPostProcessor
 *
 * @author shengjun.hu
 * @date 2022/10/8
 */
@Slf4j
public class ConfigCenterBeanPostProcessor implements BeanPostProcessor {

    private final ConfigContext configContext;

    public ConfigCenterBeanPostProcessor(ConfigContext configContext) {
        this.configContext = configContext;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Field[] declaredFields = bean.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            ConfigValue configValue = field.getAnnotation(ConfigValue.class);
            if (configValue == null) {
                continue;
            }

            // 不能是final、static的字段
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                throw new IllegalStateException(String.format("类：[%s]，字段：[%s]不能使用final、static修饰！",
                        bean.getClass().getName(), field.getName()));
            }

            String configKey = configValue.value();
            Class<? extends ValueConverter> converter = configValue.converter();
            this.injectConfigValue(configKey, converter, bean, field);
            if (configValue.refresh()) {
                this.collectRefreshConfigInfo(configKey, converter, beanName, field, null, bean);
            }
        }

        for (Method method : bean.getClass().getDeclaredMethods()) {
            ConfigListener configListener = method.getAnnotation(ConfigListener.class);
            if (configListener == null) {
                continue;
            }
            if (Modifier.isStatic(method.getModifiers()) || Modifier.isFinal(method.getModifiers())) {
                throw new IllegalStateException(String.format("类：[%s]，方法：[%s]不能使用final、static修饰！",
                        bean.getClass().getName(), method.getName()));
            }
            if (method.getParameterCount() != 1 || !String.class.equals(method.getParameterTypes()[0])) {
                throw new IllegalStateException(String.format("类：[%s]，方法：[%s]仅能定义一个String类型参数！",
                        bean.getClass().getName(), method.getName()));
            }
            if (!void.class.equals(method.getReturnType())) {
                throw new IllegalStateException(String.format("类：[%s]，方法：[%s]返回值必须为空！",
                        bean.getClass().getName(), method.getName()));
            }
            this.invokeMethod(configListener.value(), bean, method);
            this.collectRefreshConfigInfo(configListener.value(), null, beanName, null, method, bean);
        }

        return bean;
    }

    /**
     * 注入值
     * @param configKey
     * @param converter
     * @param bean
     * @param field
     */
    private void injectConfigValue(String configKey, Class<? extends ValueConverter> converter, Object bean, Field field) {
        Value value = field.getAnnotation(Value.class);
        if (value != null) {
            // 使用spring的value注解后，不进行注入，由spring进行注入
            return;
        }
        AppConfigInfo appConfigInfo = configContext.getConfigInfo(configKey);
        if (appConfigInfo == null) {
            if (configContext.isCheckConfigExist()) {
                throw new IllegalArgumentException(String.format("key: [%s]，未在配置中心配置！", configKey));
            }
            log.warn("key: [{}]，未在配置中心配置！", configKey);
            return;
        }
        String configValue = appConfigInfo.getValue();
        try {
            Object targetValue = Convertions.convertValueToTargetType(configValue, field.getType(), converter.newInstance(),
                    ReflectUtils.getGenericClasses(field));
            ReflectUtils.setValue(bean, field, targetValue);
            log.info("类：[{}]，字段：[{}]，key: [{}]，注入值：[{}]完成", bean.getClass().getName(), field.getName(), configKey, configValue);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 调用ListenConfig的方法
     * @param configKey
     * @param bean
     * @param method
     */
    private void invokeMethod(String configKey, Object bean, Method method) {
        AppConfigInfo appConfigInfo = configContext.getConfigInfo(configKey);
        if (appConfigInfo == null) {
            if (configContext.isCheckConfigExist()) {
                throw new IllegalArgumentException(String.format("key: [%s]，未在配置中心配置！", configKey));
            }
            log.warn("key: [{}]，未在配置中心配置！", configKey);
            return;
        }
        String configValue = appConfigInfo.getValue();
        try {
            ReflectUtils.invokeMethod(bean, method, configValue);
            log.info("类：[{}]，方法：[{}]，key: [{}]，value：[{}]，调用成功", bean.getClass().getName(), method.getName(), configKey, configValue);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 收集引用的动态字段或方法
     * @param configKey
     * @param beanName
     * @param field
     */
    private void collectRefreshConfigInfo(String configKey, Class<? extends ValueConverter> converter, String beanName, Field field, Method method, Object bean) {
        RefreshConfigRefInfo refreshConfigRefInfo = new RefreshConfigRefInfo();
        refreshConfigRefInfo.setKey(configKey);
        refreshConfigRefInfo.setConverter(converter);
        refreshConfigRefInfo.setField(field);
        refreshConfigRefInfo.setMethod(method);
        refreshConfigRefInfo.setBeanName(beanName);
        refreshConfigRefInfo.setBean(bean);

        configContext.addRefreshConfigRefInfo(refreshConfigRefInfo);
    }

}
