package com.hcc.config.center.client.annotation;

import com.hcc.config.center.client.config.ConfigCenterConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启用配置注解
 *
 * @author hushengjun
 * @date 2022/10/14
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(ConfigCenterConfig.class)
public @interface EnableConfigCenter {
}
