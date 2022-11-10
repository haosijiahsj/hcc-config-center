package com.hcc.config.center.client.entity;

import lombok.Data;

/**
 * 配置变更事件
 *
 * @author shengjun.hu
 * @date 2022/11/10
 */
@Data
public class ConfigChangeEvent {

    private String key;
    private MsgEventType eventType;
    private String oldValue;
    private String newValue;

}
