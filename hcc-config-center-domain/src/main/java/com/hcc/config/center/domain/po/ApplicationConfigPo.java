package com.hcc.config.center.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 配置信息
 *
 * @author hushengjun
 * @date 2022/10/6
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("application_config")
public class ApplicationConfigPo extends BasePo {

    private Long applicationId;
    private String key;
    private String value;
    private String comment;
    private Boolean dynamic;
    private Boolean allowModify;
    private Integer version;

}
