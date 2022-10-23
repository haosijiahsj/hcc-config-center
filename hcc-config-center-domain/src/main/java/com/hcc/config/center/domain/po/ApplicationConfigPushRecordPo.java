package com.hcc.config.center.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ApplicationConfigPushRecordPo
 *
 * @author hushengjun
 * @date 2022/10/21
 */
@Data
@TableName("application_config_push_record")
public class ApplicationConfigPushRecordPo {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long applicationConfigId;
    @TableField("_value")
    private String value;
    private Integer version;
    private LocalDateTime createTime;

}
