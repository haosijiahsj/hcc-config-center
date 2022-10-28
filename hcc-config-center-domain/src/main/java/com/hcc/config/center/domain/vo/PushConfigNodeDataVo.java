package com.hcc.config.center.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * PushConfigNodeDataVo
 *
 * @author shengjun.hu
 * @date 2022/10/19
 */
@Data
public class PushConfigNodeDataVo {

    private String msgType;
    private List<String> clientIds;
    private String appCode;
    private String appMode;
    private String key;
    private String value;
    private Integer version;
    private Boolean forceUpdate;

}
