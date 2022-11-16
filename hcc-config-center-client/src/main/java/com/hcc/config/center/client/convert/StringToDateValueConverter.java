package com.hcc.config.center.client.convert;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * String -> Date
 *
 * @author shengjun.hu
 * @date 2022/11/4
 */
public class StringToDateValueConverter implements ValueConverter<Date> {

    @Override
    public Date convert(String value, Class targetClass) {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        Date date;
        try {
            if (value.contains("-")) {
                if (value.contains(":")) {
                    date = dateTimeFormat.parse(value);
                } else {
                    date = dateFormat.parse(value);
                }
            }
            else if (value.contains(":")) {
                date = timeFormat.parse(value);
            }
            else {
                date = new Date(Long.parseLong(value));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("无法将值：[%s]转换到目标类型：[%s]", value, targetClass), e);
        }

        return date;
    }

}
