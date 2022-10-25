package com.hcc.config.center.domain.vo;

import com.hcc.config.center.domain.enums.AppModeEnum;
import com.hcc.config.center.domain.enums.AppStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ApplicationVo
 *
 * @author hushengjun
 * @date 2022/10/14
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ApplicationVo extends BaseVo {

    private String appCode;
    private String appName;
    private String secretKey;
    private String appMode;
    private String appStatus;
    private String owner;

    public String getAppStatusDesc() {
        AppStatusEnum appStatusEnum = AppStatusEnum.getByName(appStatus);
        return appStatusEnum == null ? null : appStatusEnum.getDesc();
    }

    public String getAppModeDesc() {
        AppModeEnum appModeEnum = AppModeEnum.getByName(this.appMode);
        return appModeEnum == null ? null : appModeEnum.getDesc();
    }

}
