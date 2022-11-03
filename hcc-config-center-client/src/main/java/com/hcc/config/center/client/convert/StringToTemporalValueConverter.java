package com.hcc.config.center.client.convert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;

/**
 * String -> LocalDateTime, LocalDate, LocalTime
 *
 * @author shengjun.hu
 * @date 2022/11/3
 */
public class StringToTemporalValueConverter implements ValueConverter<Temporal> {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public Temporal convert(String value, Class<? extends Temporal> targetClass) {
        try {
            if (LocalDateTime.class.equals(targetClass)) {
                return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
            } else if (LocalDate.class.equals(targetClass)) {
                return LocalDate.parse(value, DATE_FORMATTER);
            } else if (LocalTime.class.equals(targetClass)) {
                return LocalTime.parse(value, TIME_FORMATTER);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("无法将值：[%s]转换到目标类型：[%s]", value, targetClass), e);
        }

        throw new IllegalArgumentException(String.format("不支持的日期类型：[%s]", targetClass));
    }

}
