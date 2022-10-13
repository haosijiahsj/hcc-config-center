package com.hcc.config.center.client.netty.handler;

import com.hcc.config.center.client.context.ConfigCenterContext;
import com.hcc.config.center.client.entity.AppConfigInfo;
import com.hcc.config.center.client.entity.DynamicFieldInfo;
import com.hcc.config.center.client.utils.ConvertUtils;
import com.hcc.config.center.client.utils.JsonUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
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

    public void startWorker() {
        Runnable runnable = () -> {
            while (true) {
                try {
                    MsgInfo msgInfo = blockingQueue.take();

                    String key = msgInfo.getKey();
                    List<DynamicFieldInfo> dynamicFieldInfos = keyDynamicFieldInfo.get(key);

                    if (CollectionUtils.isEmpty(dynamicFieldInfos)) {
                        continue;
                    }

                    // 当前版本>=服务器版本 且 非强制更新 则不更新
                    Integer curVersion = configCenterContext.getKeyVersion(key);
                    if (curVersion != null && curVersion >= msgInfo.getVersion() && !msgInfo.getForceUpdate()) {
                        continue;
                    }

                    // 当前值与服务器值一致则不更新
                    String curValue = configCenterContext.getKeyValue(key);
                    if (curValue != null && curValue.equals(msgInfo.getValue())) {
                        continue;
                    }

                    for (DynamicFieldInfo dynamicFieldInfo : dynamicFieldInfos) {
                        Field field = dynamicFieldInfo.getField();
                        String value = msgInfo.getValue();
                        Object bean = dynamicFieldInfo.getBean();

                        field.setAccessible(true);
                        field.set(bean, ConvertUtils.convertValueToTargetType(value, field.getType()));
                    }
                    configCenterContext.refreshConfigMap(key, msgInfo);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        };

        Thread thread = new Thread(runnable, "config-center-refresh");
        thread.start();
    }

    public void addMsg(String msg) {
        if (msg == null) {
            return;
        }
        MsgInfo msgInfo = JsonUtils.toObject(msg, MsgInfo.class);
        blockingQueue.add(msgInfo);
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    private static class MsgInfo extends AppConfigInfo {
        private String clientId;
        private String appCode;
        private Boolean forceUpdate;
    }

}
