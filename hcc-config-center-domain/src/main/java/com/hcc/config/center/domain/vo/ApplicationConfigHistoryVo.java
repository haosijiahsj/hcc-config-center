package com.hcc.config.center.domain.vo;

import com.hcc.config.center.domain.enums.AppConfigOperateTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 配置修改记录
 *
 * @author hushengjun
 * @date 2022/10/6
 */
@Data
public class ApplicationConfigHistoryVo {

    private Long id;
    private Long applicationConfigId;
    private String value;
    private Integer version;
    private String operateType;
    private LocalDateTime createTime;

    public String getOperateTypeDesc() {
        AppConfigOperateTypeEnum appConfigOperateTypeEnum = AppConfigOperateTypeEnum.valueOf(operateType);
        return null;
    }
}
