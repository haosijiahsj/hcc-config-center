package com.hcc.config.center.domain.param;

import lombok.Data;
import org.springframework.util.Assert;

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

    public void check() {
        Assert.isTrue(applicationId != null, "应用id不能为空");
        Assert.isTrue(key != null && !"".equals(key), "key不能为空");
        Assert.isTrue(value != null, "value不能为空");
    }

    public String getKey() {
        return key.trim();
    }
}
