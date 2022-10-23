package com.hcc.config.center.client.entity;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * CallListenConfigMethodFailedInfo
 *
 * @author hushengjun
 * @date 2022/10/21
 */
@Data
public class CallListenConfigMethodFailed {

    private Class<?> clazz;
    private Method method;
    private String key;
    private String oldValue;
    private String newValue;
    private Exception exception;

}
