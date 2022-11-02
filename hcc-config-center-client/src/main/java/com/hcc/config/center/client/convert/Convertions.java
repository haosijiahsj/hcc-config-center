package com.hcc.config.center.client.convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/**
 * ConvertUtils
 *
 * @author shengjun.hu
 * @date 2022/10/13
 */
public class Convertions {

    // 支持类型
    private static final List<Class<?>> charTypes;
    private static final List<Class<?>> booleanTypes;
    private static final List<Class<?>> numberTypes;

    // 内置converter
    private static final ValueConverter<?> stringToEnumValueConverter = new StringToEnumValueConverter();
    private static final ValueConverter<?> stringToBooleanValueConverter = new StringToBooleanValueConverter();
    private static final ValueConverter<?> stringToCharacterValueConverter = new StringToCharacterValueConverter();
    private static final ValueConverter<?> stringToNumberValueConverter = new StringToNumberValueConverter();

    static {
        charTypes = Arrays.asList(
                char.class, Character.class
        );
        booleanTypes = Arrays.asList(
                boolean.class, Boolean.class
        );
        numberTypes = Arrays.asList(
                byte.class, short.class, int.class, float.class, double.class, long.class,
                Byte.class, Short.class, Integer.class, Float.class, Double.class, Long.class,
                BigInteger.class, BigDecimal.class
        );
    }

    /**
     * 选择converter
     * @param targetClass
     * @return
     */
    private static ValueConverter<?> selectConverter(Class<?> targetClass) {
        if (targetClass.isEnum()) {
            return stringToEnumValueConverter;
        } else if (charTypes.contains(targetClass)) {
            return stringToCharacterValueConverter;
        } else if (booleanTypes.contains(targetClass)) {
            return stringToBooleanValueConverter;
        } else if (numberTypes.contains(targetClass)) {
            return stringToNumberValueConverter;
        }

        return null;
    }

    /**
     * 转换到对象
     * @param value
     * @param targetClass
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T convertValueToTargetObject(String value, Class<T> targetClass) {
        Object targetValue = convertValueToTargetType(value, targetClass);
        if (targetValue == null) {
            return null;
        }

        return (T) targetValue;
    }

    @SuppressWarnings("unchecked")
    public static <T> T convertValueToTargetObject(String value, Class<T> targetClass, ValueConverter valueConverter) {
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
     * @return
     */
    public static Object convertValueToTargetType(String value, Class<?> targetClass) {
        if (value == null || String.class.equals(targetClass)) {
            return value;
        }

        ValueConverter<?> valueConverter = selectConverter(targetClass);
        if (valueConverter == null) {
            throw new IllegalStateException(String.format("目标类型：[%s]没有默认转换器", targetClass));
        }

        return convertValueToTargetType(value, targetClass, valueConverter);
    }

    @SuppressWarnings("unchecked")
    public static Object convertValueToTargetType(String value, Class<?> targetClass, ValueConverter valueConverter) {
        if (value == null || String.class.equals(targetClass)) {
            return value;
        }

        return valueConverter.convert(value, targetClass);
    }

}
