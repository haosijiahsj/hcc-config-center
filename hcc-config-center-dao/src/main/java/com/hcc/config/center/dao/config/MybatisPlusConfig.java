package com.hcc.config.center.dao.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MybatisPlusConfig
 *
 * @author hushengjun
 * @date 2022/10/6
 */
@Configuration
@MapperScan("com.hcc.config.center.dao.mapper")
public class MybatisPlusConfig {
}
