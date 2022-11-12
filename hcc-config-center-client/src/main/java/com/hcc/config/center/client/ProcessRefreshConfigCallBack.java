package com.hcc.config.center.client;

import com.hcc.config.center.client.entity.ProcessRefreshConfigInfo;

/**
 * ConfigValue,ConfigListener注解处理后回调接口<br/>
 * 定义类实现后暴露为bean即可
 *
 * @author hushengjun
 * @date 2022/10/21
 */
public interface ProcessRefreshConfigCallBack {

    /**
     * 处理动态值成功回调方法
     * @param info
     */
    default void onSuccess(ProcessRefreshConfigInfo info) {}
    /**
     * 处理动态值失败回调方法
     * @param info
     * @param e
     */
    default void onException(ProcessRefreshConfigInfo info, Exception e) {}

}
