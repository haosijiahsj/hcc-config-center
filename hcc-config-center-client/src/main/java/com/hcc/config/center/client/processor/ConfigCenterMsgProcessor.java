package com.hcc.config.center.client.processor;

import com.hcc.config.center.client.ProcessFailedCallBack;
import com.hcc.config.center.client.context.ConfigContext;
import com.hcc.config.center.client.entity.AppConfigInfo;
import com.hcc.config.center.client.entity.CallListenConfigMethodFailed;
import com.hcc.config.center.client.entity.DynamicFieldInfo;
import com.hcc.config.center.client.entity.ListenConfigMethodInfo;
import com.hcc.config.center.client.entity.MsgInfo;
import com.hcc.config.center.client.entity.UpdateFieldFailed;
import com.hcc.config.center.client.utils.ConvertUtils;
import lombok.extern.slf4j.Slf4j;
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
    private final Map<String, List<DynamicFieldInfo>> keyDynamicFieldInfoMap;
    private final Map<String, List<ListenConfigMethodInfo>> keyListenConfigMethodInfoMap;

    public ConfigCenterMsgProcessor(ConfigContext configContext, ProcessFailedCallBack callBack) {
        this.configContext = configContext;
        this.callBack = callBack;
        keyDynamicFieldInfoMap = configContext.getDynamicFieldInfos()
                .stream()
                .collect(Collectors.groupingBy(DynamicFieldInfo::getKey));
        keyListenConfigMethodInfoMap = configContext.getListenConfigMethodInfos()
                .stream()
                .collect(Collectors.groupingBy(ListenConfigMethodInfo::getKey));
        this.startWorker();
    }

    /**
     * 获取队列头一个值
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

        if (!this.needProcess(msgInfo)) {
            return;
        }

        String key = msgInfo.getKey();

        // DynamicValue注解处理
        List<DynamicFieldInfo> dynamicFieldInfos = keyDynamicFieldInfoMap.get(key);
        if (!CollectionUtils.isEmpty(dynamicFieldInfos)) {
            dynamicFieldInfos.forEach(dynamicFieldInfo -> this.processDynamicValue(msgInfo, dynamicFieldInfo));
        } else {
            if (log.isDebugEnabled()) {
                // 没有动态字段
                log.debug("key: [{}]没有字段引用，忽略更新", key);
            }
        }

        // ListenConfig注解处理
        List<ListenConfigMethodInfo> listenConfigMethodInfos = keyListenConfigMethodInfoMap.get(key);
        if (!CollectionUtils.isEmpty(listenConfigMethodInfos)) {
            listenConfigMethodInfos.forEach(methodInfo -> this.processListenConfig(msgInfo, methodInfo));
        } else {
            if (log.isDebugEnabled()) {
                log.warn("key: [{}]没有方法标注，忽略调用", key);
            }
        }
    }

    /**
     * 处理动态字段
     * @param msgInfo
     * @param dynamicFieldInfo
     */
    private void processDynamicValue(MsgInfo msgInfo, DynamicFieldInfo dynamicFieldInfo) {
        String key = msgInfo.getKey();
        String newValue = msgInfo.getValue();
        Integer newVersion = msgInfo.getVersion();
        Boolean forceUpdate = msgInfo.getForceUpdate();

        Field field = dynamicFieldInfo.getField();
        Object bean = dynamicFieldInfo.getBean();
        Integer dynCurVersion = dynamicFieldInfo.getVersion();
        String dynCurValue = dynamicFieldInfo.getValue();
        if (dynCurVersion != null && dynCurVersion >= newVersion && !forceUpdate) {
            if (log.isDebugEnabled()) {
                log.debug("类：[{}]，字段：[{}]，key: [{}]，value: [{}]，当前版本[{}]>=服务器版本[{}]，忽略更新",
                        bean.getClass().getName(), field.getName(), key, newValue, dynCurVersion, newVersion);
            }
            dynamicFieldInfo.setVersion(newVersion);
            return;
        }

        if ((dynCurValue == null && newValue == null) || (dynCurValue != null && dynCurValue.equals(newValue))) {
            if (log.isDebugEnabled()) {
                log.debug("类：[{}]，字段：[{}]，key: [{}]，当前值[{}]与服务器值[{}]一致，忽略更新",
                        bean.getClass().getName(), field.getName(), key, dynCurValue, newValue);
            }
            return;
        }

        try {
            field.setAccessible(true);
            field.set(bean, ConvertUtils.convertValueToTargetType(newValue, field.getType()));

            dynamicFieldInfo.setVersion(newVersion);
            dynamicFieldInfo.setValue(newValue);
            log.info("类：[{}]，字段：[{}]，key: [{}]，更新值：[{}]完成", bean.getClass().getName(), field.getName(), key, newValue);
        } catch (Exception e) {
            log.error(String.format("类：[%s]，字段：[%s]，key: [%s]，更新值：[%s]异常！", bean.getClass().getName(), field.getName(), key, newValue), e);

            UpdateFieldFailed failedInfo = new UpdateFieldFailed();
            failedInfo.setClazz(bean.getClass());
            failedInfo.setField(field);
            failedInfo.setKey(key);
            failedInfo.setOldValue(dynCurValue);
            failedInfo.setNewValue(newValue);
            callBack.updateFieldFailedCallBack(failedInfo, e);
        }
    }

    /**
     * 处理ListenConfig方法
     * @param msgInfo
     * @param methodInfo
     */
    private void processListenConfig(MsgInfo msgInfo, ListenConfigMethodInfo methodInfo) {
        String key = msgInfo.getKey();
        String newValue = msgInfo.getValue();
        Integer newVersion = msgInfo.getVersion();
        Boolean forceUpdate = msgInfo.getForceUpdate();

        Method method = methodInfo.getMethod();
        Object bean = methodInfo.getBean();
        Integer dynCurVersion = methodInfo.getVersion();
        String dynCurValue = methodInfo.getValue();
        if (dynCurVersion != null && dynCurVersion >= newVersion && !forceUpdate) {
            if (log.isDebugEnabled()) {
                log.debug("类：[{}]，方法：[{}]，key: [{}]，value: [{}]，当前版本[{}]>=服务器版本[{}]，忽略调用",
                        bean.getClass().getName(), method.getName(), key, newValue, dynCurVersion, newVersion);
            }
            methodInfo.setVersion(newVersion);
            return;
        }

        if ((dynCurValue == null && newValue == null) || (dynCurValue != null && dynCurValue.equals(newValue))) {
            if (log.isDebugEnabled()) {
                log.debug("类：[{}]，方法：[{}]，key: [{}]，当前值[{}]与服务器值[{}]一致，忽略调用",
                        bean.getClass().getName(), method.getName(), key, dynCurValue, newValue);
            }
            return;
        }

        try {
            method.invoke(bean, newValue);
            methodInfo.setVersion(newVersion);
            methodInfo.setValue(newValue);
            log.info("类：[{}]，方法：[{}]，key: [{}]，value：[{}]，调用完成", bean.getClass().getName(), method.getName(), key, newValue);
        } catch (Exception e) {
            log.error(String.format("类：[%s]，方法：[%s]，key: [%s]，value：[%s]，调用异常！", bean.getClass().getName(), method.getName(), key, newValue), e);

            CallListenConfigMethodFailed failedInfo = new CallListenConfigMethodFailed();
            failedInfo.setClazz(bean.getClass());
            failedInfo.setMethod(method);
            failedInfo.setKey(key);
            failedInfo.setOldValue(dynCurValue);
            failedInfo.setNewValue(newValue);
            callBack.callListenConfigMethodFailedCallBack(failedInfo, e);
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

        // 本地值刷新
        AppConfigInfo appConfigInfo = configContext.getConfigInfo(key);
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
