package com.hcc.config.center.client.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 监听配置值变更，作用于方法，方法不能用static final修饰，必须返回void，仅有一个String参数<br/>
 * {@literal @}ConfigListener("your config key")<br/>
 * private void configListener(String value) {}
 *
 * @author hushengjun
 * @date 2022/10/21
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigListener {

    String value();

}
