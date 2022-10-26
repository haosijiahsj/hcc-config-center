package com.hcc.config.center.client;

import com.hcc.config.center.client.entity.ProcessDynamicConfigFailed;

/**
 * 动态配置处理失败后回调方法<br/>
 * 定义类实现后暴露为bean即可
 *
 * @author hushengjun
 * @date 2022/10/21
 */
public interface ProcessFailedCallBack {

    /**
     * 处理动态值失败回调方法
     * @param processDynamicConfigFailed
     */
    default void callBack(ProcessDynamicConfigFailed processDynamicConfigFailed, Exception exception) {}

}
