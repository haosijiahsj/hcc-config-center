package com.hcc.config.center.client.convert;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * String -> Number
 *
 * @author shengjun.hu
 * @date 2022/11/2
 */
public class StringToNumberValueConverter implements ValueConverter<Number> {

    @Override
    public Number convert(String value, Class<? extends Number> targetClass) {
        Number targetValue;
        if (Byte.class.equals(targetClass) || byte.class.equals(targetClass)) {
            targetValue = Byte.valueOf(value);
        }
        else if (Short.class.equals(targetClass) || short.class.equals(targetClass)) {
            targetValue = Short.valueOf(value);
        }
        else if (Integer.class.equals(targetClass) || int.class.equals(targetClass)) {
            targetValue = Integer.valueOf(value);
        }
        else if (Long.class.equals(targetClass) || long.class.equals(targetClass)) {
            targetValue = Long.valueOf(value);
        }
        else if (Float.class.equals(targetClass) || float.class.equals(targetClass)) {
            targetValue = Float.valueOf(value);
        }
        else if (Double.class.equals(targetClass) || double.class.equals(targetClass)) {
            targetValue = Double.valueOf(value);
        }
        else if (BigInteger.class.equals(targetClass)) {
            targetValue = new BigInteger(value);
        }
        else if (BigDecimal.class.equals(targetClass)) {
            targetValue = new BigDecimal(value);
        }
        else {
            throw new IllegalArgumentException(String.format("无法将值：[%s]转换到目标类型：[%s]", value, targetClass));
        }

        return targetValue;
    }

}
