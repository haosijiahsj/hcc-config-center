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
public class PushConfigClientMsgVo {

    private String msgType;
    private String clientId;
    private String appCode;
    private String key;
    private String value;
    private Integer version;
    private Boolean forceUpdate;

}
