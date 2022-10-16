package com.hcc.config.center.domain.vo;

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
    private String appStatus;
    private String owner;

}
