package com.hcc.config.center.client.convert;

import com.hcc.config.center.client.utils.JsonUtils;
import com.hcc.config.center.client.utils.StrUtils;

/**
 * String(json) -> Object
 *
 * @author hushengjun
 * @date 2022/11/20
 */
public class StringToObjectValueConverter implements ValueConverter<Object> {

    @Override
    public Object convert(String value, Class<?> targetClass) {
        if (StrUtils.isEmpty(value)) {
            return null;
        }
        try {
            return JsonUtils.toObject(value, targetClass);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("无法将值：[%s]转换到目标类型：[%s]", value, targetClass), e);
        }
    }

}
