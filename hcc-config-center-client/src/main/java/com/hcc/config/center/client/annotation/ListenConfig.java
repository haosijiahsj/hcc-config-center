package com.hcc.config.center.client.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 监听配置值变更，作用于方法
 *
 * @author hushengjun
 * @date 2022/10/21
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ListenConfig {

    String value();

}
