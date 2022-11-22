package com.hcc.config.center.client.convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * ConverterFactory
 *
 * @author shengjun.hu
 * @date 2022/11/16
 */
public class ConverterFactory {

    // 支持类型
    private static final List<Class<?>> charTypes;
    private static final List<Class<?>> booleanTypes;
    private static final List<Class<?>> numberTypes;

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
    public static ValueConverter<?> selectConverter(Class<?> targetClass) {
        if (targetClass == null) {
            throw new IllegalArgumentException("目标类型不能为空");
        }

        ValueConverter<?> valueConverter = new NoOpValueConverter();
        if (charTypes.contains(targetClass)) {
            // char和包装char
            valueConverter = new StringToCharacterValueConverter();
        }
        else if (booleanTypes.contains(targetClass)) {
            // boolean和包装boolean
            valueConverter = new StringToBooleanValueConverter();
        }
        else if (numberTypes.contains(targetClass)) {
            // 常用数字类型
            valueConverter = new StringToNumberValueConverter();
        }
        else if (targetClass.isEnum()) {
            // 枚举类型
            valueConverter = new StringToEnumValueConverter();
        }
        else if (Temporal.class.isAssignableFrom(targetClass)) {
            // java8日期
            valueConverter = new StringToTemporalValueConverter();
        }
        else if (Date.class.equals(targetClass)) {
            // 普通日期
            valueConverter = new StringToDateValueConverter();
        }
        else if (IConvertObject.class.isAssignableFrom(targetClass)) {
            // 实现IConvertObject的对象，使用json转换
            valueConverter = new StringToObjectValueConverter();
        }

        return valueConverter;
    }

}
