package com.hcc.config.center.client.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 发送给服务端的消息
 *
 * @author hushengjun
 * @date 2023/1/1
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SendServerMsg extends BaseMsg {
}
