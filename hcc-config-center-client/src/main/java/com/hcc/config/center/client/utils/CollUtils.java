package com.hcc.config.center.client.utils;

import java.util.Collection;
import java.util.Map;

/**
 * 集合工具类
 *
 * @author shengjun.hu
 * @date 2022/11/16
 */
public class CollUtils {

    private CollUtils() {}

    public static boolean isEmpty(Collection<?> coll) {
        return coll == null || coll.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> coll) {
        return !isEmpty(coll);
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

}
