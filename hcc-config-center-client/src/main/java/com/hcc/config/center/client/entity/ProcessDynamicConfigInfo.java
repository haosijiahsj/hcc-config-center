package com.hcc.config.center.client.entity;

import lombok.Builder;
import lombok.Data;

/**
 * 处理的动态配置信息，作为回调接口参数
 *
 * @author hushengjun
 * @date 2022/10/21
 */
@Data
@Builder
public class ProcessDynamicConfigInfo {

    private String key;
    private Integer oldVersion;
    private Integer newVersion;
    private String oldValue;
    private String newValue;

}
