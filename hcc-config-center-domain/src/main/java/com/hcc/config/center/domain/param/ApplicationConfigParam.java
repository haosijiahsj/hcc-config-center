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
    private Boolean dynamic;

    public String getKey() {
        if (key == null) {
            throw new IllegalArgumentException("key不能为空");
        }
        return key;
    }
}
