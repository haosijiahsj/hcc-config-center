package com.hcc.config.center.client.entity;

import lombok.Data;

import java.lang.reflect.Field;

/**
 * UpdateFieldFailedInfo
 *
 * @author hushengjun
 * @date 2022/10/21
 */
@Data
public class UpdateFieldFailed {

    private Class<?> clazz;
    private Field field;
    private String key;
    private String oldValue;
    private String newValue;
    private Exception exception;

}
