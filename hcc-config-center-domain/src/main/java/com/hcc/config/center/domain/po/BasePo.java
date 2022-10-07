package com.hcc.config.center.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * BasePo
 *
 * @author hushengjun
 * @date 2022/10/6
 */
@Data
public class BasePo {

    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;

}
