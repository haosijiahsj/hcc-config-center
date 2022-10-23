package com.hcc.config.center.client;

import com.hcc.config.center.client.entity.CallListenConfigMethodFailed;
import com.hcc.config.center.client.entity.UpdateFieldFailed;

/**
 * 动态配置处理失败后回调方法<br/>
 * 定义类实现后暴露为bean即可
 *
 * @author hushengjun
 * @date 2022/10/21
 */
public interface ProcessFailedCallBack {

    /**
     * 更新动态值失败回调方法
     * @param fieldFailed
     */
    default void updateFieldFailedCallBack(UpdateFieldFailed fieldFailed) {}

    /**
     * 调用监听配置失败回调方法
     * @param methodFailed
     */
    default void callListenConfigMethodFailedCallBack(CallListenConfigMethodFailed methodFailed) {}

}
