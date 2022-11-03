package com.hcc.config.center.service.context;

import lombok.Data;

/**
 * MsgInfo
 *
 * @author hushengjun
 * @date 2022/10/14
 */
@Data
public class MsgInfo {

    private String msgType;
    private String appCode;

    public enum MsgType {
        INIT
    }

}
