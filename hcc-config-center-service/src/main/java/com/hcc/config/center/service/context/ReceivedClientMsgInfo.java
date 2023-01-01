package com.hcc.config.center.service.context;

import com.hcc.config.center.domain.vo.BaseMsg;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收到的客户端消息
 *
 * @author hushengjun
 * @date 2022/10/14
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ReceivedClientMsgInfo extends BaseMsg {

    public enum MsgType {
        INIT
    }

}
