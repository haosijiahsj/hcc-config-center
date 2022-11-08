package com.hcc.config.center.client.entity;

import com.hcc.config.center.client.convert.ValueConverter;
import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 刷新的配置引用信息
 *
 * @author shengjun.hu
 * @date 2022/10/8
 */
@Data
public class RefreshConfigRefInfo {

    private String beanName;
    private Object bean;
    private Field field;
    private Method method;
    private Class<? extends ValueConverter> converter;

    private String key;

}
