package com.hcc.config.center.client.convert.converter;

import com.hcc.config.center.client.convert.ValueConverter;

/**
 * 作为默认值
 *
 * @author shengjun.hu
 * @date 2022/11/2
 */
public class NoOpValueConverter implements ValueConverter<String> {

    @Override
    public String convert(String value, Class targetClass) {
        throw new UnsupportedOperationException(String.format("值：[%s]转换到目标类型：[%s]，未找到默认转换器，请指定转换器！", value, targetClass));
    }

}
