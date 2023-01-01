package com.hcc.config.center.domain.vo;

import lombok.Data;

/**
 * 消息基类
 *
 * @author hushengjun
 * @date 2023/1/1
 */
@Data
public class BaseMsg {

    private String msgType;
    private String clientId;
    private String appCode;

}
