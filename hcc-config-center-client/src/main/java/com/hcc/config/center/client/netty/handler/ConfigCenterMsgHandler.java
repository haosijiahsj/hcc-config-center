package com.hcc.config.center.client.netty.handler;

import com.hcc.config.center.client.context.ConfigCenterContext;
import com.hcc.config.center.client.entity.AppConfigInfo;
import com.hcc.config.center.client.entity.DynamicFieldInfo;
import com.hcc.config.center.client.entity.MsgInfo;
import com.hcc.config.center.client.utils.ConvertUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * ConfigCenterMsgHandler
 *
 * @author shengjun.hu
 * @date 2022/10/13
 */
@Slf4j
public class ConfigCenterMsgHandler {

    private final BlockingQueue<MsgInfo> blockingQueue = new LinkedBlockingQueue<>(20);

    private final ConfigCenterContext configCenterContext;
    private final Map<String, List<DynamicFieldInfo>> keyDynamicFieldInfo;

    public ConfigCenterMsgHandler(ConfigCenterContext configCenterContext) {
        this.configCenterContext = configCenterContext;
        keyDynamicFieldInfo = configCenterContext.getDynamicFieldInfos()
                .stream()
                .collect(Collectors.groupingBy(DynamicFieldInfo::getKey));
        this.startWorker();
    }

    private MsgInfo takeMsgInfo() {
        try {
            return blockingQueue.take();
        } catch (InterruptedException e) {
            throw new IllegalStateException("获取队列值异常", e);
        }
    }

    /**
     * 启动一个线程来更新动态值
     */
    public void startWorker() {
        Runnable runnable = () -> {
            while (true) {
                MsgInfo msgInfo = takeMsgInfo();

                String key = msgInfo.getKey();
                String newValue = msgInfo.getValue();
                Integer newVersion = msgInfo.getVersion();
                Boolean forceUpdate = msgInfo.getForceUpdate();
                List<DynamicFieldInfo> dynamicFieldInfos = keyDynamicFieldInfo.get(key);

                if (!this.needUpdate(msgInfo)) {
                    continue;
                }

                for (DynamicFieldInfo dynamicFieldInfo : dynamicFieldInfos) {
                    Field field = dynamicFieldInfo.getField();
                    Object bean = dynamicFieldInfo.getBean();
                    Integer dynCurVersion = dynamicFieldInfo.getVersion();
                    String dynCurValue = dynamicFieldInfo.getValue();
                    if (dynCurVersion != null && dynCurVersion >= newVersion && !forceUpdate) {
                        log.info("类：[{}]，字段：[{}]，key: [{}]，value: [{}]，当前版本[{}]>=服务器版本[{}]，忽略更新",
                                bean.getClass().getName(), field.getName(), key, newValue, dynCurVersion, newVersion);
                        dynamicFieldInfo.setVersion(newVersion);
                        continue;
                    }

                    if (dynCurValue != null && dynCurValue.equals(newValue)) {
                        log.info("类：[{}]，字段：[{}]，key: [{}]，当前值[{}]与服务器值[{}]一致，忽略更新",
                                bean.getClass().getName(), field.getName(), key, dynCurValue, newValue);
                        continue;
                    }

                    try {
                        field.setAccessible(true);
                        field.set(bean, ConvertUtils.convertValueToTargetType(newValue, field.getType()));

                        dynamicFieldInfo.setVersion(newVersion);
                        dynamicFieldInfo.setValue(newValue);
                        log.info("类：[{}]，字段：[{}]，key: [{}]，更新值：[{}]完成", bean.getClass().getName(), field.getName(), key, newValue);
                    } catch (Exception e) {
                        log.error(String.format("类：[%s]，字段：[%s]，key: [%s]，更新值：[%s]异常！", bean.getClass().getName(), field.getName(), key, newValue), e);
                    }
                }
            }
        };

        new Thread(runnable, "value-refresh").start();
    }

    /**
     * 是否能够更新
     * @param msgInfo
     * @return
     */
    private boolean needUpdate(MsgInfo msgInfo) {
        String key = msgInfo.getKey();
        String newValue = msgInfo.getValue();
        Integer newVersion = msgInfo.getVersion();

        // 本地值刷新
        AppConfigInfo appConfigInfo = configCenterContext.getKeyConfig(key);
        appConfigInfo.setValue(newValue);
        appConfigInfo.setVersion(newVersion);
        configCenterContext.refreshConfigMap(key, appConfigInfo);

        // 没有动态字段
        if (CollectionUtils.isEmpty(keyDynamicFieldInfo.get(key))) {
            log.warn("key: [{}]没有字段引用，忽略更新", key);
            return false;
        }

        // appCode不一致
        String curAppCode = configCenterContext.getAppCode();
        if (!curAppCode.equals(msgInfo.getAppCode())) {
            log.warn("当前appCode: [{}]与消息appCode: [{}]不匹配，忽略更新", curAppCode, msgInfo.getAppCode());
            return false;
        }

        return true;
    }

    /**
     * 添加消息到队列
     * @param msgInfo
     */
    public void addMsgToQueue(MsgInfo msgInfo) {
        if (msgInfo == null) {
            return;
        }
        blockingQueue.add(msgInfo);
    }

    public String getAppCode() {
        return configCenterContext.getAppCode();
    }

}
