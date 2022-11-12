package com.hcc.config.center.client;

import com.hcc.config.center.client.entity.ConfigRefreshInfo;

/**
 * ConfigValue,ConfigListener注解处理后回调接口<br/>
 * 定义类实现后暴露为bean即可
 *
 * @author hushengjun
 * @date 2022/10/21
 */
public interface ConfigRefreshCallBack {

    /**
     * 处理动态值成功回调方法，不要在此执行耗时操作
     * @param info
     */
    default void onSuccess(ConfigRefreshInfo info) {}

    /**
     * 处理动态值失败回调方法，不要在此执行耗时操作
     * @param info
     * @param e
     */
    default void onException(ConfigRefreshInfo info, Exception e) {}

}
