package com.hcc.config.center.domain.enums;

/**
 * AppStatusEnum
 *
 * @author hushengjun
 * @date 2022/10/7
 */
public enum AppStatusEnum {

    NOT_ONLINE("未上线"),
    ONLINE("已上线"),
    OFFLINE("已下线");

    private final String desc;

    AppStatusEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public static AppStatusEnum getByName(String name) {
        if (name == null) {
            return null;
        }
        for (AppStatusEnum appStatusEnum : AppStatusEnum.values()) {
            if (appStatusEnum.name().equals(name)) {
                return appStatusEnum;
            }
        }

        return null;
    }

}
