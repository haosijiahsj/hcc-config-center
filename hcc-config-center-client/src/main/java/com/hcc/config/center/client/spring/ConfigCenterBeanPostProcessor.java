package com.hcc.config.center.client.spring;

import com.hcc.config.center.client.annotation.DynamicValue;
import com.hcc.config.center.client.annotation.ListenConfig;
import com.hcc.config.center.client.annotation.StaticValue;
import com.hcc.config.center.client.context.ConfigContext;
import com.hcc.config.center.client.entity.AppConfigInfo;
import com.hcc.config.center.client.entity.DynamicFieldInfo;
import com.hcc.config.center.client.entity.ListenConfigMethodInfo;
import com.hcc.config.center.client.utils.ConvertUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;

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
            StaticValue staticValue = field.getAnnotation(StaticValue.class);
            DynamicValue dynamicValue = field.getAnnotation(DynamicValue.class);
            if (staticValue == null && dynamicValue == null) {
                continue;
            }

            // 不能是final、static的字段
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                throw new IllegalStateException(String.format("类：[%s]，字段：[%s]不能使用final、static修饰！",
                        bean.getClass().getName(), field.getName()));
            }

            String configKey = staticValue != null ? staticValue.value() : dynamicValue.value();
            this.injectConfigValue(configKey, bean, field);
            if (dynamicValue != null) {
                this.collectDynamicFieldInfo(configKey, beanName, field, bean);
            }
        }

        for (Method method : bean.getClass().getDeclaredMethods()) {
            ListenConfig listenConfig = method.getAnnotation(ListenConfig.class);
            if (listenConfig == null) {
                continue;
            }
            if (Modifier.isPrivate(method.getModifiers()) || Modifier.isStatic(method.getModifiers())
                    || Modifier.isFinal(method.getModifiers())) {
                throw new IllegalStateException(String.format("类：[%s]，方法：[%s]不能使用private、final、static修饰！",
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
            this.invokeMethod(listenConfig.value(), bean, method);
            this.collectListenConfigMethodInfo(listenConfig.value(), beanName, method, bean);
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
        AppConfigInfo appConfigInfo = configContext.getConfigInfo(configKey);
        if (appConfigInfo == null) {
            log.warn("key: [{}]，未在配置中心配置！", configKey);
            return;
        }
        String configValue = appConfigInfo.getValue();
        try {
            field.setAccessible(true);
            field.set(bean, ConvertUtils.convertValueToTargetType(configValue, field.getType()));
            log.info("类：[{}]，字段：[{}]，key: [{}]，注入值：[{}]完成", bean.getClass().getName(), field.getName(), configKey, configValue);
        } catch (IllegalAccessException e) {
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
            log.warn("key: [{}]，未在配置中心配置！", configKey);
            return;
        }
        String configValue = appConfigInfo.getValue();
        try {
            method.invoke(bean, configValue);
            log.info("类：[{}]，方法：[{}]，key: [{}]，value：[{}]，调用成功", bean.getClass().getName(), method.getName(), configKey, configValue);
        } catch (Exception e) {
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

        // 将value和version放进去
        AppConfigInfo appConfigInfo = configContext.getConfigInfo(configKey);
        if (appConfigInfo != null) {
            if (!appConfigInfo.getDynamic()) {
                log.warn("类：[{}]，字段：[{}]，key: [{}]，不是动态配置！", bean.getClass().getName(), field.getName(), configKey);
            }

            dynamicFieldInfo.setValue(appConfigInfo.getValue());
            dynamicFieldInfo.setVersion(appConfigInfo.getVersion());
        }

        configContext.addDynamicFieldInfo(dynamicFieldInfo);
    }

    /**
     * 收集需要动态字段刷新后调用的方法
     * @param configKey
     * @param beanName
     * @param method
     */
    private void collectListenConfigMethodInfo(String configKey, String beanName, Method method, Object bean) {
        ListenConfigMethodInfo methodInfo = new ListenConfigMethodInfo();
        methodInfo.setKey(configKey);
        methodInfo.setMethod(method);
        methodInfo.setBeanName(beanName);
        methodInfo.setBean(bean);
        methodInfo.setBeanClass(bean.getClass());

        // 将value和version放进去
        AppConfigInfo appConfigInfo = configContext.getConfigInfo(configKey);
        if (appConfigInfo != null) {
            if (!appConfigInfo.getDynamic()) {
                log.warn("类：[{}]，方法：[{}]，key: [{}]，不是动态配置！", bean.getClass().getName(), method.getName(), configKey);
            }

            methodInfo.setValue(appConfigInfo.getValue());
            methodInfo.setVersion(appConfigInfo.getVersion());
        }

        configContext.addListenConfigMethodInfo(methodInfo);
    }

}
