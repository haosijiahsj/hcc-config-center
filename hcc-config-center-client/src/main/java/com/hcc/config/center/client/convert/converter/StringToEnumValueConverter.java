package com.hcc.config.center.client.convert.converter;

import com.hcc.config.center.client.convert.IConvertEnum;
import com.hcc.config.center.client.convert.ValueConverter;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * String -> Enum
 *
 * @author shengjun.hu
 * @date 2022/11/2
 */
public class StringToEnumValueConverter implements ValueConverter<Enum> {

    @Override
    public Enum convert(String value, Class<? extends Enum> targetClass) {
        try {
            if (!IConvertEnum.class.isAssignableFrom(targetClass)) {
                return Enum.valueOf(targetClass, value);
            }

            // 实现了IConvertEnum接口
            Enum[] enumConstants = targetClass.getEnumConstants();
            for (Enum enumConstant : enumConstants) {
                Serializable enumValue = ((IConvertEnum) enumConstant).getValue();
                if (enumValue != null) {
                    if (enumValue instanceof BigDecimal && new BigDecimal(value).compareTo((BigDecimal) enumValue) == 0) {
                        return enumConstant;
                    }
                    else if (value.equals(enumValue.toString())) {
                        return enumConstant;
                    }
                }
            }

            throw new IllegalArgumentException(String.format("值：[%s]转换到目标类型：[%s]失败", value, targetClass));
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("无法将值：[%s]转换到目标类型：[%s]", value, targetClass), e);
        }
    }

}
