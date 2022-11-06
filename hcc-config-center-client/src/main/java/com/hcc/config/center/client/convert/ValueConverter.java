package com.hcc.config.center.client.convert;

/**
 * 值转换器，实现此接口并在注解中指定以使用自定义转换器
 *
 * @author shengjun.hu
 * @date 2022/11/2
 */
@FunctionalInterface
public interface ValueConverter<T> {

    /**
     * 转换方法
     * @param value 不为空
     * @param targetClass 目标类型
     * @return
     */
    T convert(String value, Class<? extends T> targetClass);

}
