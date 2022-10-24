package com.hcc.config.center.domain.enums;

/**
 * AppMode
 *
 * @author shengjun.hu
 * @date 2022/10/24
 */
public enum AppMode {

    PUSH("服务器推送模式"),
    PULL("客户端拉取模式");

    private final String desc;

    AppMode(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
