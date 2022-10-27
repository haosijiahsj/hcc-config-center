package com.hcc.config.center.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * ApplicationConfigPushRecordPo
 *
 * @author hushengjun
 * @date 2022/10/21
 */
@Data
public class ApplicationConfigPushRecordVo {

    private Long id;
    private Long applicationConfigId;
    private String value;
    private Integer version;
    private LocalDateTime createTime;

}
