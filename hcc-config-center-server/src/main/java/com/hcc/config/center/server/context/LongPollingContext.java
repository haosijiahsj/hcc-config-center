package com.hcc.config.center.server.context;

import com.hcc.config.center.domain.vo.AppConfigInfo;
import lombok.Data;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LongPollingManage
 *
 * @author shengjun.hu
 * @date 2022/10/25
 */
public class LongPollingContext {

    private static final Map<String, ConnectEntry> clientIdConnectEntryMap = new ConcurrentHashMap<>();

    public synchronized static void add(String clientId, String appCode, DeferredResult<List<AppConfigInfo>> result, List<String> keys) {
        ConnectEntry connectEntry = new ConnectEntry();
        connectEntry.setClientId(clientId);
        connectEntry.setAppCode(appCode);
        connectEntry.setResult(result);
        connectEntry.setSubKeys(keys);

        clientIdConnectEntryMap.put(clientId, connectEntry);
    }

    public synchronized static void remove(String clientId) {
        clientIdConnectEntryMap.remove(clientId);
    }

    public synchronized static void publish(AppConfigInfo appConfigInfo) {
        Collection<ConnectEntry> values = clientIdConnectEntryMap.values();

        List<String> removeClientIds = new ArrayList<>();
        for (ConnectEntry entry : values) {
            if (entry.getAppCode().equals(appConfigInfo.getAppCode())
                    && entry.getSubKeys().contains(appConfigInfo.getKey())) {
                entry.getResult().setResult(Collections.singletonList(appConfigInfo));
                removeClientIds.add(entry.getClientId());
            }
        }
        removeClientIds.forEach(clientIdConnectEntryMap::remove);
    }

    @Data
    private static class ConnectEntry {
        private String clientId;
        private String appCode;
        private DeferredResult<List<AppConfigInfo>> result;
        private List<String> subKeys;
    }

}
