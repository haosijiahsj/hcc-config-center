package com.hcc.config.center.client.entity;

import lombok.Data;

/**
 * MsgInfo
 *
 * @author shengjun.hu
 * @date 2022/10/14
 */
@Data
public class MsgInfo {

    private String msgType;
    private String clientId;
    private String appCode;
    private String key;
    private String value;
    private Integer version;
    private Boolean forceUpdate;

}
