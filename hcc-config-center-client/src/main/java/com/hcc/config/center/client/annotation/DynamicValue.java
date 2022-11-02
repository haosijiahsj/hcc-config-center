package com.hcc.config.center.client.annotation;

import com.hcc.config.center.client.convert.NoOpValueConverter;
import com.hcc.config.center.client.convert.ValueConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 动态刷新注解，作用于字段
 *
 * @author hushengjun
 * @date 2022/10/6
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DynamicValue {
    /**
     * 配置key
     * @return
     */
    String value();
    /**
     * 自定义的转换器
     * @return
     */
    Class<? extends ValueConverter> converter() default NoOpValueConverter.class;

}
