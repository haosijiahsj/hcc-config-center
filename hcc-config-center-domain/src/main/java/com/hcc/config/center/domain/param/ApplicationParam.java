package com.hcc.config.center.domain.param;

import lombok.Data;
import org.springframework.util.Assert;

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
    private String appMode;
    private String owner;

    public void check() {
        Assert.isTrue(appCode != null && !"".equals(appCode), "应用编码不能为空");
        Assert.isTrue(appName != null, "应用名称不能为空");
    }

    public String getAppCode() {
        return appCode.trim();
    }

}
