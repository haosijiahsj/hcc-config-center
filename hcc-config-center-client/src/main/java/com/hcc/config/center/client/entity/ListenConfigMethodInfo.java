package com.hcc.config.center.client.entity;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * ListenConfigMethodInfo
 *
 * @author hushengjun
 * @date 2022/10/21
 */
@Data
public class ListenConfigMethodInfo {

    private String key;
    private String beanName;
    private Object bean;
    private Method method;
    private Class<?> beanClass;
    private String value;
    private Integer version;

}
