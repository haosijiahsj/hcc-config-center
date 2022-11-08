package com.hcc.config.center.client.annotation;

import com.hcc.config.center.client.convert.NoOpValueConverter;
import com.hcc.config.center.client.convert.ValueConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 应用配置中心值的注解，作用于字段，若要获得刷新能力，需要指定refresh=true<br/>
 * 支持以下类型自动转换<br/>
 * String, 8种基础数据类型（包装+原始）, BigInteger, BigDecimal, Enum, java.util.Date, LocalDateTime, LocalDate, LocalTime<br/>
 * 自定义类型需要指定converter的class
 * @see com.hcc.config.center.client.convert.ValueConverter
 *
 * @author hushengjun
 * @date 2022/10/6
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigValue {
    /**
     * 配置key
     * @return
     */
    String value();
    /**
     * 是否动态刷新
     * @return
     */
    boolean refresh() default false;
    /**
     * 自定义的转换器
     * @return
     *
     */
    Class<? extends ValueConverter> converter() default NoOpValueConverter.class;

}
