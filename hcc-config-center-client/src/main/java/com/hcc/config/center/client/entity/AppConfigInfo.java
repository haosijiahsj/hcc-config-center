package com.hcc.config.center.client.entity;

import lombok.Data;

/**
 * 配置中心值
 *
 * @author hushengjun
 * @date 2022/10/7
 */
@Data
public class AppConfigInfo {

    private String key;
    private String value;
    private Boolean dynamic;
    private Integer version;

}
