package com.hcc.config.center.domain.param;

import lombok.Data;

/**
 * ApplicationParam
 *
 * @author hushengjun
 * @date 2022/10/16
 */
@Data
public class ApplicationParam {

    private Long id;
    private String appCode;
    private String appName;
    private String owner;

    public String getAppCode() {
        if (appCode == null) {
            throw new IllegalArgumentException("应用编码不能为空");
        }
        return appCode;
    }

    public String getAppName() {
        if (appName == null) {
            throw new IllegalArgumentException("应用名称不能为空");
        }
        return appName;
    }

}
