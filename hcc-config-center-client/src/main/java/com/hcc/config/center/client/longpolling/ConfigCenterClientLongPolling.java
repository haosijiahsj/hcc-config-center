package com.hcc.config.center.client.longpolling;

import com.hcc.config.center.client.ConfigRefreshCallBack;
import com.hcc.config.center.client.context.ConfigContext;
import com.hcc.config.center.client.entity.ReceivedServerMsg;
import com.hcc.config.center.client.processor.ConfigCenterMsgProcessor;
import com.hcc.config.center.client.utils.CollUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 定时拉取任务
 *
 * @author shengjun.hu
 * @date 2022/10/24
 */
@Slf4j
public class ConfigCenterClientLongPolling {

    private final ConfigContext configContext;
    private final ConfigCenterMsgProcessor configCenterMsgProcessor;

    public ConfigCenterClientLongPolling(ConfigContext configContext, ConfigRefreshCallBack callBack) {
        this.configContext = configContext;
        this.configCenterMsgProcessor = new ConfigCenterMsgProcessor(configContext, callBack);
    }

    /**
     * 启动定时拉取任务
     */
    public void startUp() {
        this.startUpLongPolling();
        this.startUpTask();
    }

    /**
     * 启动长轮询
     */
    private void startUpLongPolling() {
        Runnable longPollingRunnable = () -> {
            while (true) {
                if (log.isDebugEnabled()) {
                    log.debug("开始建立长连接拉取变更配置任务！");
                }
                try {
                    this.doPullConfigAndProcess(true);
                } catch (Exception e) {
                    log.error("本次拉取处理任务出现异常！休眠10s", e);
                    this.sleepForSecond(10);
                }
            }
        };

        new Thread(longPollingRunnable, "long-polling").start();
    }

    /**
     * 启动补偿任务
     */
    private void startUpTask() {
        Runnable runnable = () -> {
            while (true) {
                this.sleepForSecond(configContext.getPullInterval());
                if (log.isDebugEnabled()) {
                    log.debug("开始执行拉取变更配置任务！");
                }
                try {
                    this.doPullConfigAndProcess(false);
                } catch (Exception e) {
                    log.error("本次拉取处理任务出现异常！", e);
                }

                // 休眠等待下次执行
                if (log.isDebugEnabled()) {
                    log.debug("本次任务处理完成！休眠{}s后执行！", configContext.getPullInterval());
                }
            }
        };

        new Thread(runnable, "pull-task").start();
    }

    /**
     * 拉取配置并处理
     */
    private void doPullConfigAndProcess(boolean isLongPolling) {
        // 拉取配置中心所有动态配置
        List<ReceivedServerMsg> receivedServerMsgs;
        if (isLongPolling) {
            receivedServerMsgs = configContext.longPolling();
        } else {
            receivedServerMsgs = configContext.getChangedConfigFromConfigCenter();
        }
        if (CollUtils.isEmpty(receivedServerMsgs)) {
            if (log.isDebugEnabled()) {
                log.debug("配置中心配置未发生变更，等待下次执行");
            }
            return;
        }

        // 添加到处理队列
        receivedServerMsgs.forEach(configCenterMsgProcessor::addMsgToQueue);
    }

    /**
     * 休眠
     * @param timeout
     */
    private void sleepForSecond(long timeout) {
        try {
            TimeUnit.SECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            log.error("sleep异常！", e);
        }
    }

}
