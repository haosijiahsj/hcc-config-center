package com.hcc.config.center.domain.enums;

/**
 * AppStatusEnum
 *
 * @author hushengjun
 * @date 2022/10/7
 */
public enum AppStatusEnum {

    NOT_RELEASE("未发布"),
    HAVE_RELEASED("已发布");

    private final String desc;

    AppStatusEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

}
