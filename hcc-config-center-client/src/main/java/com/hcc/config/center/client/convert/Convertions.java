package com.hcc.config.center.client.convert;

import com.hcc.config.center.client.convert.converter.NoOpValueConverter;

/**
 * ConvertUtils
 *
 * @author shengjun.hu
 * @date 2022/10/13
 */
public class Convertions {

    /**
     * 转换到对象
     * @param value
     * @param targetClass
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T convertValueToTargetObject(String value, Class<T> targetClass, ValueConverter<T> valueConverter) {
        Object targetValue = convertValueToTargetType(value, targetClass, valueConverter);
        if (targetValue == null) {
            return null;
        }

        return (T) targetValue;
    }

    /**
     * 转换为目标类型
     * @param value
     * @param targetClass
     * @param valueConverter
     * @param genericClasses
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Object convertValueToTargetType(String value, Class<?> targetClass, ValueConverter valueConverter,
                                                  Class<?>...genericClasses) {
        if (value == null || String.class.equals(targetClass)) {
            return value;
        }
        if (valueConverter == null || valueConverter instanceof NoOpValueConverter) {
            valueConverter = ConverterFactory.selectConverter(targetClass, genericClasses);
        }

        return valueConverter.convert(value, targetClass);
    }

}
