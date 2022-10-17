package com.hcc.config.center.domain.param;

import lombok.Data;

/**
 * ApplicationConfigParam
 *
 * @author shengjun.hu
 * @date 2022/10/17
 */
@Data
public class ApplicationConfigParam {

    private Long id;
    private Long applicationId;
    private String key;
    private String value;
    private String comment;
    private String dynamic;

}
