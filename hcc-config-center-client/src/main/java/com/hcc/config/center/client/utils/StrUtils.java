package com.hcc.config.center.client.utils;

/**
 * 字符串工具类
 *
 * @author shengjun.hu
 * @date 2022/11/16
 */
public class StrUtils {

    private StrUtils() {}

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

}
