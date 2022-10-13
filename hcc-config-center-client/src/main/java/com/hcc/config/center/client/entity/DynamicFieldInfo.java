package com.hcc.config.center.client.entity;

import lombok.Data;

import java.lang.reflect.Field;

/**
 * RefreshValueDefine
 *
 * @author shengjun.hu
 * @date 2022/10/8
 */
@Data
public class DynamicFieldInfo {

    private String key;
    private String beanName;
    private Object bean;
    private Field field;
    private Class<?> beanClass;
    private Integer version;
    private String value;

}
