package com.hcc.config.center.client.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 从服务端收到的消息
 *
 * @author shengjun.hu
 * @date 2022/10/14
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ReceivedServerMsg extends BaseMsg {

    private String clientId;
    private String key;
    private String value;
    private Integer version;
    private Boolean forceUpdate;

}
