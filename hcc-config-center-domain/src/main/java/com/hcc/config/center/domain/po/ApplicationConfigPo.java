package com.hcc.config.center.domain.po;

import com.baomidou.mybatisplus.annotation.TableField;
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
    @TableField("_key")
    private String key;
    @TableField("_value")
    private String value;
    @TableField("_comment")
    private String comment;
    private Integer version;

}
