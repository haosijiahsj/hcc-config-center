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

}
