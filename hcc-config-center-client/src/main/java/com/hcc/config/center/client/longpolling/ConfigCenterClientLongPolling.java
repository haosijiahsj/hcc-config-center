package com.hcc.config.center.client.longpolling;

import com.hcc.config.center.client.ProcessFailedCallBack;
import com.hcc.config.center.client.context.ConfigContext;
import com.hcc.config.center.client.entity.AppConfigInfo;
import com.hcc.config.center.client.entity.MsgInfo;
import com.hcc.config.center.client.processor.ConfigCenterMsgProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    public ConfigCenterClientLongPolling(ConfigContext configContext, ProcessFailedCallBack callBack) {
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

    private void startUpLongPolling() {
        Runnable longPollingRunnable = () -> {
            while (true) {
                log.info("开始建立长连接拉取动态配置任务！");
                try {
                    this.doPullConfigAndProcess(true);
                } catch (Exception e) {
                    log.error("本次拉取处理任务出现异常！", e);
                }
            }
        };

        new Thread(longPollingRunnable, "long-polling").start();
    }

    private void startUpTask() {
        Runnable runnable = () -> {
            while (true) {
                this.sleepForSecond(configContext.getPullInterval());
                log.info("开始执行拉取动态配置任务！");
                try {
                    this.doPullConfigAndProcess(false);
                } catch (Exception e) {
                    log.error("本次拉取处理任务出现异常！", e);
                }

                // 休眠等待下次执行
                log.info("本次任务处理完成！休眠{}s后执行！", configContext.getPullInterval());
            }
        };

        new Thread(runnable, "pull-task").start();
    }

    /**
     * 拉取配置并处理
     */
    private void doPullConfigAndProcess(boolean isLongPolling) {
        // 拉取配置中心所有动态配置
        List<AppConfigInfo> dynamicAppConfigInfos;
        if (isLongPolling) {
            dynamicAppConfigInfos = configContext.longPolling();
        } else {
            dynamicAppConfigInfos = configContext.getDynamicConfigFromConfigCenter();
        }
        if (CollectionUtils.isEmpty(dynamicAppConfigInfos)) {
            log.info("配置中心动态配置为空，等待下次执行");
            return;
        }

        // 添加到处理队列
        this.convertToMsgInfo(dynamicAppConfigInfos).forEach(configCenterMsgProcessor::addMsgToQueue);
    }

    /**
     * 转换为可处理的消息
     * @param dynamicAppConfigInfos
     * @return
     */
    private List<MsgInfo> convertToMsgInfo(List<AppConfigInfo> dynamicAppConfigInfos) {
        return dynamicAppConfigInfos.stream()
                .map(c -> {
                    MsgInfo msgInfo = new MsgInfo();
                    BeanUtils.copyProperties(c, msgInfo);
                    if (c.getVersion() == 0) {
                        msgInfo.setForceUpdate(true);
                        msgInfo.setMsgType(MsgInfo.MsgType.CONFIG_DELETE.name());
                    } else {
                        msgInfo.setForceUpdate(false);
                        msgInfo.setMsgType(MsgInfo.MsgType.CONFIG_UPDATE.name());
                    }

                    return msgInfo;
                })
                .collect(Collectors.toList());
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
