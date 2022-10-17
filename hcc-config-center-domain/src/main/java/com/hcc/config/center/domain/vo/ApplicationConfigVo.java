package com.hcc.config.center.domain.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ApplicationConfigVo
 *
 * @author shengjun.hu
 * @date 2022/10/17
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ApplicationConfigVo extends BaseVo {

    private Long applicationId;
    private String key;
    private String value;
    private String comment;
    private Boolean dynamic;
    private Integer version;

}
