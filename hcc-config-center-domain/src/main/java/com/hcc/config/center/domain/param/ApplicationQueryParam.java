package com.hcc.config.center.domain.param;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ApplicationQueryParam
 *
 * @author hushengjun
 * @date 2022/10/16
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ApplicationQueryParam extends PageParam {

    private String appCode;
    private String appName;

}
