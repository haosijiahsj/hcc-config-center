package com.hcc.config.center.client.convert;

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
            return Enum.valueOf(targetClass, value);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("无法将值：[%s]转换到目标类型：[%s]", value, targetClass), e);
        }
    }

}
