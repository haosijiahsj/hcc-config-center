package com.hcc.config.center.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 应用信息
 *
 * @author hushengjun
 * @date 2022/10/6
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("application")
public class ApplicationPo extends BasePo {

    private String appCode;
    private String appName;
    private String secretKey;
    private String appMode;
    private String appStatus;
    private String owner;

}
