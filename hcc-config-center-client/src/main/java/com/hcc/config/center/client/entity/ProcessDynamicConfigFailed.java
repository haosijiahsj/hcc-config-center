package com.hcc.config.center.client.entity;

import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * UpdateFieldFailedInfo
 *
 * @author hushengjun
 * @date 2022/10/21
 */
@Data
public class ProcessDynamicConfigFailed {

    private Class<?> clazz;
    private Field field;
    private Method method;
    private String key;
    private String oldValue;
    private String newValue;

}
