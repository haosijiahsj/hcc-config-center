package com.hcc.config.center.client.utils;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * ConvertUtils
 *
 * @author shengjun.hu
 * @date 2022/10/13
 */
public class ConvertUtils {

    /**
     * 转换为目标类型
     * @param value
     * @param targetClass
     * @return
     */
    public static Object convertValueToTargetType(String value, Class<?> targetClass) {
        if (value == null) {
            return null;
        }

        if (String.class.equals(targetClass)) {
            return value;
        }

        Object targetValue;
        if (Byte.class.equals(targetClass) || byte.class.equals(targetClass)) {
            targetValue = Byte.valueOf(value);
        } else if (Short.class.equals(targetClass) || short.class.equals(targetClass)) {
            targetValue = Short.valueOf(value);
        } else if (Integer.class.equals(targetClass) || int.class.equals(targetClass)) {
            targetValue = Integer.valueOf(value);
        } else if (Long.class.equals(targetClass) || long.class.equals(targetClass)) {
            targetValue = Long.valueOf(value);
        } else if (Float.class.equals(targetClass) || float.class.equals(targetClass)) {
            targetValue = Float.valueOf(value);
        } else if (Double.class.equals(targetClass) || double.class.equals(targetClass)) {
            targetValue = Double.valueOf(value);
        } else if (Character.class.equals(targetClass) || char.class.equals(targetClass)) {
            if (value.length() > 1) {
                throw new IllegalArgumentException(String.format("值：[%s]无法转换为char", value));
            }
            targetValue = value.charAt(0);
        } else if (Boolean.class.equals(targetClass) || boolean.class.equals(targetClass)) {
            targetValue = Boolean.valueOf(value);
        } else if (BigInteger.class.equals(targetClass)) {
            targetValue = new BigInteger(value);
        } else if (BigDecimal.class.equals(targetClass)) {
            targetValue = new BigDecimal(value);
        } else {
            throw new IllegalArgumentException(String.format("无法将值：[%s]转换到目标类型：[%s]", value, targetClass));
        }

        return targetValue;
    }

}
