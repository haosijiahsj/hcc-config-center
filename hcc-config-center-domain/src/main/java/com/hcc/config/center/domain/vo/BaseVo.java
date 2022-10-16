package com.hcc.config.center.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * BasePo
 *
 * @author hushengjun
 * @date 2022/10/6
 */
@Data
public class BaseVo {

    private Long id;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;

}
