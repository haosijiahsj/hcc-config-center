package com.hcc.config.center.domain.enums;

/**
 * AppMode
 *
 * @author shengjun.hu
 * @date 2022/10/24
 */
public enum AppModeEnum {

    LONG_CONNECT("长连接"),
    LONG_POLLING("长轮询");

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
