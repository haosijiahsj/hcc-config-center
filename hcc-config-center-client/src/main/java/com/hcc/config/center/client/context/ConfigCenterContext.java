package com.hcc.config.center.client.context;

import com.hcc.config.center.client.constant.Constants;
import com.hcc.config.center.client.entity.AppConfigInfo;
import com.hcc.config.center.client.entity.DynamicFieldInfo;
import com.hcc.config.center.client.utils.RestTemplateUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ConfigCenterContext
 *
 * @author hushengjun
 * @date 2022/10/6
 */
@Data
@NoArgsConstructor
public class ConfigCenterContext {

    private String appCode;
    private String secretKey;
    private String serverUrl;
    private Integer serverPort;
    private boolean enableDynamicPush = false;

    private Map<String, AppConfigInfo> configMap = new HashMap<>();
    private Map<String, String> configKeyValueMap = new HashMap<>();
    private List<DynamicFieldInfo> dynamicFieldInfos = new ArrayList<>();

    /**
     * 配置中心获取配置地址
     * @return
     */
    public String getConfigCenterGetConfigUrl() {
        return this.getConfigCenterUrl() + Constants.APP_CONFIG_URI;
    }

    public String getConfigCenterServerNodeUrl() {
        return this.getConfigCenterUrl() + Constants.SERVER_NODE_URI;
    }

    private String getConfigCenterUrl() {
        String urlPlaceHolder = "%s";
        if (serverPort != null) {
            urlPlaceHolder = urlPlaceHolder + ":" + serverPort;
        }

        return String.format(urlPlaceHolder, serverUrl);
    }

    /**
     * 初始化上下文，从配置中心获取配置
     */
    public void initContext() {
        List<AppConfigInfo> appConfigInfos = this.getConfigFromConfigCenter();
        for (AppConfigInfo appConfigInfo : appConfigInfos) {
            this.configKeyValueMap.put(appConfigInfo.getKey(), appConfigInfo.getValue());
            this.configMap.put(appConfigInfo.getKey(), appConfigInfo);
        }
    }

    /**
     * 从配置中心拉取应用的所有配置
     * @return
     */
    private List<AppConfigInfo> getConfigFromConfigCenter() {
        String configCenterUrl = this.getConfigCenterGetConfigUrl();

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("appCode", appCode);
        paramMap.put("secretKey", secretKey);

        return RestTemplateUtils.getAppConfig(configCenterUrl, paramMap);
    }

    public synchronized void addDynamicFieldInfo(DynamicFieldInfo dynamicFieldInfo) {
        dynamicFieldInfos.add(dynamicFieldInfo);
    }

    /**
     * 刷新配置
     * @param key
     * @param appConfigInfo
     */
    public synchronized void refreshConfigMap(String key, AppConfigInfo appConfigInfo) {
        configMap.put(key, appConfigInfo);
        configKeyValueMap.put(key, appConfigInfo.getValue());
    }

    /**
     * 获取指定key的版本
     * @param key
     * @return
     */
    public Integer getKeyVersion(String key) {
        AppConfigInfo appConfigInfo = configMap.get(key);
        if (appConfigInfo == null) {
            return null;
        }

        return appConfigInfo.getVersion();
    }

    public String getKeyValue(String key) {
        return configKeyValueMap.get(key);
    }

    public AppConfigInfo getKeyConfig(String key) {
        return configMap.get(key);
    }

}
