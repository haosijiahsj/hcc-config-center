package com.hcc.config.center.domain.param;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ApplicationConfigParam
 *
 * @author shengjun.hu
 * @date 2022/10/17
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ApplicationConfigQueryParam extends PageParam {

    private Long applicationId;
    private String key;
    private Boolean dynamic;

}
