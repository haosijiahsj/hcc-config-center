package com.hcc.config.center.client.entity;

import lombok.Data;

/**
 * AppConfig
 *
 * @author hushengjun
 * @date 2022/10/7
 */
@Data
public class AppConfig {

    private String key;
    private String value;
    private Boolean dynamic;

}
