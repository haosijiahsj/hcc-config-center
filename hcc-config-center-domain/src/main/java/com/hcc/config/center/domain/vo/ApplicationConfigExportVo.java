package com.hcc.config.center.domain.vo;

import lombok.Data;

/**
 * ApplicationConfigVo
 *
 * @author shengjun.hu
 * @date 2022/10/17
 */
@Data
public class ApplicationConfigExportVo {

    private String key;
    private String value;
    private String comment;
    private Boolean dynamic;

}
