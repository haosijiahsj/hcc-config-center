package com.hcc.config.center.client.processor;

import com.hcc.config.center.client.ProcessFailedCallBack;
import com.hcc.config.center.client.context.ConfigContext;
import com.hcc.config.center.client.convert.NoOpValueConverter;
import com.hcc.config.center.client.convert.ValueConverter;
import com.hcc.config.center.client.entity.AppConfigInfo;
import com.hcc.config.center.client.entity.DynamicConfigRefInfo;
import com.hcc.config.center.client.entity.MsgInfo;
import com.hcc.config.center.client.entity.ProcessDynamicConfigFailed;
import com.hcc.config.center.client.convert.Convertions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * 配置中心消息处理器
 *
 * @author shengjun.hu
 * @date 2022/10/13
 */
@Slf4j
public class ConfigCenterMsgProcessor {

    private final BlockingQueue<MsgInfo> blockingQueue = new LinkedBlockingQueue<>(20);

    private final ConfigContext configContext;
    private final ProcessFailedCallBack callBack;
    private final Map<String, List<DynamicConfigRefInfo>> keyDynamicConfigRefInfoMap;

    public ConfigCenterMsgProcessor(ConfigContext configContext, ProcessFailedCallBack callBack) {
        this.configContext = configContext;
        this.callBack = callBack;
        keyDynamicConfigRefInfoMap = configContext.getDynamicConfigRefInfos()
                .stream()
                .collect(Collectors.groupingBy(DynamicConfigRefInfo::getKey));
        this.startWorker();
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
        AppConfigInfo oldConfigInfo = null;
        if (appConfigInfo != null) {
            oldConfigInfo = new AppConfigInfo();
            BeanUtils.copyProperties(appConfigInfo, oldConfigInfo);
        }
        if (!this.needProcess(msgInfo)) {
            return;
        }

        // 处理动态配置
        List<DynamicConfigRefInfo> dynamicConfigRefInfos = keyDynamicConfigRefInfoMap.get(key);
        if (CollectionUtils.isEmpty(dynamicConfigRefInfos)) {
            if (log.isDebugEnabled()) {
                // 没有动态字段
                log.debug("key: [{}]没有字段或方法引用，忽略处理", key);
            }
            return;
        }

        AppConfigInfo finalOldConfigInfo = oldConfigInfo;
        dynamicConfigRefInfos.forEach(d -> this.processDynamicConfig(msgInfo, finalOldConfigInfo, d));
    }

    /**
     * 处理动态字段
     * @param msgInfo
     * @param appConfigInfo
     * @param dynamicConfigRefInfo
     */
    private void processDynamicConfig(MsgInfo msgInfo, AppConfigInfo appConfigInfo, DynamicConfigRefInfo dynamicConfigRefInfo) {
        String key = msgInfo.getKey();
        String newValue = msgInfo.getValue();

        Field field = dynamicConfigRefInfo.getField();
        Method method = dynamicConfigRefInfo.getMethod();
        Object bean = dynamicConfigRefInfo.getBean();

        String tmpTag = field != null ? "字段" : method != null ? "方法" : "";
        String name = field != null ? field.getName() : method != null ? method.getName() : "";

        try {
            if (field != null) {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                Class<? extends ValueConverter> converter = dynamicConfigRefInfo.getConverter();
                Object targetValue;
                if (NoOpValueConverter.class.equals(converter)) {
                    targetValue = Convertions.convertValueToTargetType(newValue, field.getType());
                } else {
                    ValueConverter valueConverter = converter.newInstance();
                    targetValue = Convertions.convertValueToTargetType(newValue, field.getType(), valueConverter);
                }
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
        } catch (Exception e) {
            log.error(String.format("类：[%s]，%s：[%s]，key: [%s]，value: [%s]反射处理异常！", bean.getClass().getName(), tmpTag, name, key, newValue), e);

            ProcessDynamicConfigFailed failedInfo = new ProcessDynamicConfigFailed();
            failedInfo.setClazz(bean.getClass());
            failedInfo.setField(field);
            failedInfo.setMethod(method);
            failedInfo.setKey(key);
            failedInfo.setOldValue(appConfigInfo == null ? null : appConfigInfo.getValue());
            failedInfo.setNewValue(newValue);
            callBack.callBack(failedInfo, e);
        }
    }

    /**
     * 是否能够更新
     * @param msgInfo
     * @return
     */
    private boolean needProcess(MsgInfo msgInfo) {
        String key = msgInfo.getKey();
        String newValue = msgInfo.getValue();
        Integer newVersion = msgInfo.getVersion();

        // appCode不一致
        String curAppCode = configContext.getAppCode();
        if (!curAppCode.equals(msgInfo.getAppCode())) {
            log.warn("当前appCode: [{}]与消息appCode: [{}]不匹配，忽略更新", curAppCode, msgInfo.getAppCode());
            return false;
        }

        AppConfigInfo appConfigInfo = configContext.getConfigInfo(key);
        if (appConfigInfo != null && appConfigInfo.getVersion() >= newVersion && !msgInfo.getForceUpdate()) {
            log.warn("key: [{}]当前版本：[{}] >= 服务器版本：[{}]，忽略更新", key, appConfigInfo.getVersion(), newVersion);
            return false;
        }

        // 本地值刷新
        if (appConfigInfo == null || appConfigInfo.getVersion() < newVersion) {
            if (appConfigInfo == null) {
                appConfigInfo = new AppConfigInfo();
                appConfigInfo.setKey(key);
                appConfigInfo.setDynamic(true);
            }
            appConfigInfo.setValue(newValue);
            appConfigInfo.setVersion(newVersion);
            configContext.refreshConfigMap(key, appConfigInfo);
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
        return configContext.getAppCode();
    }

}
