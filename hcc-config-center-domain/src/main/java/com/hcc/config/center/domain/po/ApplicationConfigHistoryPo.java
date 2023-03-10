package com.hcc.config.center.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 配置修改记录
 *
 * @author hushengjun
 * @date 2022/10/6
 */
@Data
@TableName("application_config_history")
public class ApplicationConfigHistoryPo {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long applicationConfigId;
    @TableField("_value")
    private String value;
    private Integer version;
    private String operateType;
    private LocalDateTime createTime;

}
