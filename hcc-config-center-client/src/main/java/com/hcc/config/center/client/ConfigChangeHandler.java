package com.hcc.config.center.client;

import com.hcc.config.center.client.entity.ConfigChangeEvent;

import java.util.List;

/**
 * 配置变更接口，实现接口，暴露为bean即可
 *
 * @author shengjun.hu
 * @date 2022/11/10
 */
public interface ConfigChangeHandler {

    /**
     * 关注的key
     * @return
     */
    List<String> keys();

    /**
     * 配置变更事件，不要在此执行耗时操作
     * @param event
     */
    void onChange(ConfigChangeEvent event);

    /**
     * onChange方法执行异常，不要在此执行耗时操作
     */
    default void exceptionCaught(ConfigChangeEvent event, Exception e) {
        e.printStackTrace();
    }

}
