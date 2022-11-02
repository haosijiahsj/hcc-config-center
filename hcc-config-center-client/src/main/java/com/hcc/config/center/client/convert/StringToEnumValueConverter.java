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
        return Enum.valueOf(targetClass, value);
    }

}
