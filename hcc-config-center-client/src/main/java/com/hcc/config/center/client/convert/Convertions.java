package com.hcc.config.center.client.convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Date;
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
    private static final ValueConverter<?> stringToTemporalValueConverter = new StringToTemporalValueConverter();
    private static final ValueConverter<?> stringToDateValueConverter = new StringToDateValueConverter();
    private static final ValueConverter<?> noOpValueConverter = new NoOpValueConverter();

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
        } else if (Temporal.class.isAssignableFrom(targetClass)) {
            return stringToTemporalValueConverter;
        } else if (Date.class.equals(targetClass)) {
            return stringToDateValueConverter;
        }

        return noOpValueConverter;
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
        Object targetValue = convertValueToTargetType(value, targetClass, null);
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
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Object convertValueToTargetType(String value, Class<?> targetClass, ValueConverter valueConverter) {
        if (value == null || String.class.equals(targetClass)) {
            return value;
        }
        if (valueConverter == null || valueConverter instanceof NoOpValueConverter) {
            valueConverter = selectConverter(targetClass);
        }

        return valueConverter.convert(value, targetClass);
    }

}
