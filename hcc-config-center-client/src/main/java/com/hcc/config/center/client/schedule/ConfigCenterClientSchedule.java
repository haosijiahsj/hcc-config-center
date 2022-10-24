package com.hcc.config.center.client.schedule;

import com.hcc.config.center.client.ProcessFailedCallBack;
import com.hcc.config.center.client.context.ConfigContext;
import com.hcc.config.center.client.entity.AppConfigInfo;
import com.hcc.config.center.client.entity.DynamicFieldInfo;
import com.hcc.config.center.client.entity.ListenConfigMethodInfo;
import com.hcc.config.center.client.entity.MsgInfo;
import com.hcc.config.center.client.processor.ConfigCenterMsgProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 定时拉取任务
 *
 * @author shengjun.hu
 * @date 2022/10/24
 */
@Slf4j
public class ConfigCenterClientSchedule {

    private final ConfigContext configContext;
    private final ConfigCenterMsgProcessor configCenterMsgProcessor;

    public ConfigCenterClientSchedule(ConfigContext configContext, ProcessFailedCallBack callBack) {
        this.configContext = configContext;
        this.configCenterMsgProcessor = new ConfigCenterMsgProcessor(configContext, callBack);
    }

    /**
     * 启动定时拉取任务
     */
    public void startUp() {
        Runnable runnable = () -> {
            while (true) {
                this.sleepForSecond(configContext.getPullInterval());
                log.info("开始执行拉取动态配置任务！");
                try {
                    this.doPullConfigAndProcess();
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
    private void doPullConfigAndProcess() {
        // 拉取配置中心所有动态配置
        List<AppConfigInfo> dynamicAppConfigInfos = configContext.getDynamicConfigFromConfigCenter();
        if (CollectionUtils.isEmpty(dynamicAppConfigInfos)) {
            log.info("配置中心动态配置为空，等待下次执行");
            return;
        }

        // 转换为MsgInfo
        List<MsgInfo> msgInfos = this.convertToMsgInfo(dynamicAppConfigInfos);
        if (CollectionUtils.isEmpty(msgInfos)) {
            log.info("转换后的配置为空，等待下次执行");
            return;
        }

        // 添加到处理队列
        msgInfos.forEach(configCenterMsgProcessor::addMsgToQueue);
    }

    /**
     * 转换为可处理的消息
     * @param dynamicAppConfigInfos
     * @return
     */
    private List<MsgInfo> convertToMsgInfo(List<AppConfigInfo> dynamicAppConfigInfos) {
        Map<String, AppConfigInfo> keyAppConfigInfoMap = dynamicAppConfigInfos.stream()
                .collect(Collectors.toMap(AppConfigInfo::getKey, Function.identity()));
        Map<String, MsgInfo> keyMsgInfoMap = new HashMap<>();
        for (DynamicFieldInfo fieldInfo : configContext.getDynamicFieldInfos()) {
            if (keyMsgInfoMap.containsKey(fieldInfo.getKey())) {
                continue;
            }

            AppConfigInfo appConfigInfo = keyAppConfigInfoMap.get(fieldInfo.getKey());
            keyMsgInfoMap.put(fieldInfo.getKey(), this.buildMsgInfo(fieldInfo.getKey(), appConfigInfo));
        }

        for (ListenConfigMethodInfo methodInfo : configContext.getListenConfigMethodInfos()) {
            if (keyMsgInfoMap.containsKey(methodInfo.getKey())) {
                continue;
            }

            AppConfigInfo appConfigInfo = keyAppConfigInfoMap.get(methodInfo.getKey());
            keyMsgInfoMap.put(methodInfo.getKey(), this.buildMsgInfo(methodInfo.getKey(), appConfigInfo));
        }

        return new ArrayList<>(keyMsgInfoMap.values());
    }

    private MsgInfo buildMsgInfo(String key, AppConfigInfo appConfigInfo) {
        MsgInfo msgInfo = new MsgInfo();
        msgInfo.setAppCode(configContext.getAppCode());
        msgInfo.setKey(key);
        if (appConfigInfo == null) {
            msgInfo.setMsgType(MsgInfo.MsgType.CONFIG_DELETE.name());
            msgInfo.setValue(null);
            msgInfo.setVersion(0);
            msgInfo.setForceUpdate(true);
        } else {
            msgInfo.setMsgType(MsgInfo.MsgType.CONFIG_UPDATE.name());
            msgInfo.setValue(appConfigInfo.getValue());
            msgInfo.setVersion(appConfigInfo.getVersion());
            msgInfo.setForceUpdate(false);
        }

        return msgInfo;
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
