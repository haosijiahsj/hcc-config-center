package com.hcc.config.center.client;

import com.hcc.config.center.client.entity.ProcessDynamicConfigInfo;

/**
 * 动态配置处理后回调接口<br/>
 * 定义类实现后暴露为bean即可
 *
 * @author hushengjun
 * @date 2022/10/21
 */
public interface ProcessDynamicConfigCallBack {

    /**
     * 处理动态值成功回调方法
     * @param info
     */
    default void onSuccess(ProcessDynamicConfigInfo info) {}
    /**
     * 处理动态值失败回调方法
     * @param info
     * @param e
     */
    default void onException(ProcessDynamicConfigInfo info, Exception e) {}

}
