package com.hcc.config.center.client.convert;

/**
 * 作为默认值
 *
 * @author shengjun.hu
 * @date 2022/11/2
 */
public class NoOpValueConverter implements ValueConverter<String> {

    @Override
    public String convert(String value, Class targetClass) {
        throw new UnsupportedOperationException("ooh, wrong operating !");
    }

}
