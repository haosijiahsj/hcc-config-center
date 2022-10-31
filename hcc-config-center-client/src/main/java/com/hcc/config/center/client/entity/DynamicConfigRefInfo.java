package com.hcc.config.center.client.entity;

import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 动态配置引用信息
 *
 * @author shengjun.hu
 * @date 2022/10/8
 */
@Data
public class DynamicConfigRefInfo {

    private String beanName;
    private Object bean;
    private Field field;
    private Method method;

    private String key;

}
