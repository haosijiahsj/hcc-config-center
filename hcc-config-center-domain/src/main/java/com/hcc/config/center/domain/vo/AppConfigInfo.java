package com.hcc.config.center.domain.vo;

import lombok.Data;

/**
 * AppConfigInfo
 *
 * @author shengjun.hu
 * @date 2022/10/25
 */
@Data
public class AppConfigInfo {

    private String appCode;
    private String key;
    private String value;
    private Integer version;
    private Boolean dynamic;

}
