package com.hcc.config.center.client.convert.converter;

import com.hcc.config.center.client.convert.IConvertObject;
import com.hcc.config.center.client.convert.ValueConverter;
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
            // 这里实例化一个空对象只是为了调用sourceType方法，使用反射实例化要求该对象必须有一个公共的无参构造器
            IConvertObject iConvertObj = (IConvertObject) targetClass.newInstance();
            IConvertObject.SourceType sourceType = iConvertObj.sourceType();
            if (IConvertObject.SourceType.JSON.equals(sourceType)) {
                return JsonUtils.toObject(value, targetClass);
            }

            throw new UnsupportedOperationException(String.format("不支持源类型为：[%s]的转换", sourceType));
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("无法将值：[%s]转换到目标类型：[%s]", value, targetClass), e);
        }
    }

}
