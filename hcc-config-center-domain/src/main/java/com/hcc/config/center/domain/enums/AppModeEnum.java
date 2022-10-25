package com.hcc.config.center.domain.enums;

/**
 * AppMode
 *
 * @author shengjun.hu
 * @date 2022/10/24
 */
public enum AppModeEnum {

    PUSH("服务器推送模式"),
    PULL("客户端拉取模式");

    private final String desc;

    AppModeEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public static AppModeEnum getByName(String name) {
        if (name == null) {
            return null;
        }
        for (AppModeEnum appModeEnum : AppModeEnum.values()) {
            if (appModeEnum.name().equals(name)) {
                return appModeEnum;
            }
        }

        return null;
    }

}
