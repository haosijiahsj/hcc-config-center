package com.hcc.config.center.client.convert;

/**
 * String -> Character
 *
 * @author shengjun.hu
 * @date 2022/11/2
 */
public class StringToCharacterValueConverter implements ValueConverter<Character> {

    @Override
    public Character convert(String value, Class targetClass) {
        if (value.length() > 1) {
            throw new IllegalArgumentException(String.format("值：[%s]无法转换为char", value));
        }

        return value.charAt(0);
    }

}
