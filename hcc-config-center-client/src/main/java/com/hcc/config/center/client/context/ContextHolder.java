package com.hcc.config.center.client.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ContextHolder
 *
 * @author shengjun.hu
 * @date 2022/10/8
 */
public class ContextHolder {

    private static Map<String, ConfigCenterContext> HOLDER = new ConcurrentHashMap<>();

    public static void set(String appCode, ConfigCenterContext configCenterContext) {
        HOLDER.put(appCode, configCenterContext);
    }

}
