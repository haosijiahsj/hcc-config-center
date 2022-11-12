package com.hcc.config.center.client.processor;

import com.hcc.config.center.client.ConfigChangeHandler;
import com.hcc.config.center.client.ConfigRefreshCallBack;
import com.hcc.config.center.client.context.ConfigContext;
import com.hcc.config.center.client.convert.Convertions;
import com.hcc.config.center.client.convert.ValueConverter;
import com.hcc.config.center.client.entity.AppConfigInfo;
import com.hcc.config.center.client.entity.ConfigChangeEvent;
import com.hcc.config.center.client.entity.MsgEventType;
import com.hcc.config.center.client.entity.RefreshConfigRefInfo;
import com.hcc.config.center.client.entity.MsgInfo;
import com.hcc.config.center.client.entity.ConfigRefreshInfo;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

/**
 * 配置中心消息处理器
 *
 * @author shengjun.hu
 * @date 2022/10/13
 */
@Slf4j
public class ConfigCenterMsgProcessor {

    /**
     * 存放所有需要处理的动态字段
     */
    private final BlockingQueue<MsgInfo> blockingQueue = new ArrayBlockingQueue<>(20);

    private final ConfigContext configContext;
    private final ConfigRefreshCallBack callBack;
    private final Map<String, List<RefreshConfigRefInfo>> keyRefreshConfigRefInfoMap;

    public ConfigCenterMsgProcessor(ConfigContext configContext, ConfigRefreshCallBack callBack) {
        this.configContext = configContext;
        this.callBack = callBack;
        keyRefreshConfigRefInfoMap = configContext.getRefreshConfigRefInfos()
                .stream()
                .collect(Collectors.groupingBy(RefreshConfigRefInfo::getKey));
        this.startWorker();
    }

    /**
     * 直接返回appCode，某些地方有用，避免再获取ConfigContext
     * @return
     */
    public String getAppCode() {
        return configContext.getAppCode();
    }

    /**
     * 添加消息到队列，为满阻塞
     * @param msgInfo
     */
    public void addMsgToQueue(MsgInfo msgInfo) {
        if (msgInfo == null) {
            return;
        }
        try {
            blockingQueue.put(msgInfo);
        } catch (InterruptedException e) {
            throw new IllegalStateException("添加队列值异常", e);
        }
    }

    /**
     * 获取队列头一个值，为空阻塞
     * @return
     */
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
                try {
                    this.process();
                } catch (Exception e) {
                    log.error("处理异常", e);
                }
            }
        };

        new Thread(runnable, "value-refresh").start();
    }

    /**
     * 处理入口
     */
    private void process() {
        MsgInfo msgInfo = this.takeMsgInfo();

        String key = msgInfo.getKey();
        AppConfigInfo appConfigInfo = configContext.getConfigInfo(key);
        if (!this.needProcess(msgInfo)) {
            return;
        }

        boolean processSuccess = true;
        // 处理动态配置
        for (RefreshConfigRefInfo refInfo : keyRefreshConfigRefInfoMap.getOrDefault(key, Collections.emptyList())) {
            processSuccess = this.processRefreshConfig(msgInfo, appConfigInfo, refInfo);
        }
        // 调用handler
        for (ConfigChangeHandler handler : configContext.getConfigChangeHandlers()) {
            if (handler.keys() == null || !handler.keys().contains(key)) {
                continue;
            }
            ConfigChangeEvent event = new ConfigChangeEvent();
            event.setKey(key);
            event.setEventType(MsgEventType.valueOf(msgInfo.getMsgType()));
            event.setOldValue(appConfigInfo == null ? null : appConfigInfo.getValue());
            event.setNewValue(msgInfo.getValue());

            try {
                handler.onChange(event);
            } catch (Exception e) {
                handler.exceptionCaught(event, e);
            }
        }

        // 全部成功才会刷新本地值
        if (processSuccess) {
            this.refreshConfigMap(key, appConfigInfo, msgInfo);
        }
    }

    /**
     * 更新本地缓存的值
     * @param key
     * @param appConfigInfo
     * @param msgInfo
     */
    private void refreshConfigMap(String key, AppConfigInfo appConfigInfo, MsgInfo msgInfo) {
        // 本地值刷新
        if (appConfigInfo == null || appConfigInfo.getVersion() < msgInfo.getVersion() || msgInfo.getForceUpdate()) {
            if (appConfigInfo == null) {
                appConfigInfo = new AppConfigInfo();
                appConfigInfo.setKey(key);
            }
            appConfigInfo.setValue(msgInfo.getValue());
            appConfigInfo.setVersion(msgInfo.getVersion());
            configContext.refreshConfigMap(key, appConfigInfo);
        }
    }

    /**
     * 处理需要刷新的字段或方法
     * @param msgInfo
     * @param appConfigInfo
     * @param refreshConfigRefInfo
     */
    private boolean processRefreshConfig(MsgInfo msgInfo, AppConfigInfo appConfigInfo, RefreshConfigRefInfo refreshConfigRefInfo) {
        String key = msgInfo.getKey();
        String newValue = msgInfo.getValue();

        Field field = refreshConfigRefInfo.getField();
        Method method = refreshConfigRefInfo.getMethod();
        Object bean = refreshConfigRefInfo.getBean();

        String tmpTag = field != null ? "字段" : method != null ? "方法" : "";
        String name = field != null ? field.getName() : method != null ? method.getName() : "";

        // 拼装此次处理信息
        ConfigRefreshInfo info = ConfigRefreshInfo.builder()
                .key(key)
                .version(appConfigInfo == null ? null : appConfigInfo.getVersion())
                .newVersion(msgInfo.getVersion())
                .value(appConfigInfo == null ? null : appConfigInfo.getValue())
                .newValue(newValue)
                .build();

        try {
            if (field != null) {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                Class<? extends ValueConverter> converter = refreshConfigRefInfo.getConverter();
                Object targetValue = Convertions.convertValueToTargetType(newValue, field.getType(), converter.newInstance());
                field.set(bean, targetValue);
                field.setAccessible(field.isAccessible());
            }
            if (method != null) {
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
                method.invoke(bean, newValue);
                method.setAccessible(method.isAccessible());
            }

            log.info("类：[{}]，{}：[{}]，key: [{}]，value: [{}]处理完成", bean.getClass().getName(), tmpTag, name, key, newValue);

            callBack.onSuccess(info);
        } catch (Exception e) {
            log.error(String.format("类：[%s]，%s：[%s]，key: [%s]，value: [%s]反射处理异常！", bean.getClass().getName(), tmpTag, name, key, newValue), e);

            callBack.onException(info, e);
            return false;
        }

        return true;
    }

    /**
     * 是否能够更新
     * @param msgInfo
     * @return
     */
    private boolean needProcess(MsgInfo msgInfo) {
        String key = msgInfo.getKey();
        Integer newVersion = msgInfo.getVersion();

        // appCode不一致
        String curAppCode = configContext.getAppCode();
        if (!curAppCode.equals(msgInfo.getAppCode())) {
            log.warn("当前appCode: [{}]与消息appCode: [{}]不匹配，忽略", curAppCode, msgInfo.getAppCode());
            return false;
        }

        AppConfigInfo appConfigInfo = configContext.getConfigInfo(key);
        if (appConfigInfo != null && appConfigInfo.getVersion() >= newVersion && !msgInfo.getForceUpdate()) {
            log.warn("key: [{}]当前版本：[{}] >= 服务器版本：[{}]，忽略", key, appConfigInfo.getVersion(), newVersion);
            return false;
        }

        if (keyRefreshConfigRefInfoMap.get(key) == null && configContext.getConfigChangeHandlers().isEmpty()) {
            log.warn("key: [{}]没有字段、方法标记需要刷新，没有定义ConfigChangeHandler，忽略", key);
            // 没有引用，但需要更新本地缓存
            this.refreshConfigMap(key, appConfigInfo, msgInfo);
            return false;
        }

        return true;
    }

}
