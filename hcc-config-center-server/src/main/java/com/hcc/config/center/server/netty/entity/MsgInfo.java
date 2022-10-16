package com.hcc.config.center.server.netty.entity;

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
        INIT("init");

        private String code;

        MsgType(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

}
