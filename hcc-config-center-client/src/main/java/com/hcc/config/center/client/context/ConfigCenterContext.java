package com.hcc.config.center.client.context;

import com.hcc.config.center.client.entity.AppConfig;
import com.hcc.config.center.client.utils.RestTemplateUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@AllArgsConstructor
public class ConfigCenterContext {

    private String appCode;
    private String secretKey;
    private String serverUrl;
    private Integer serverPort;

    private Map<String, AppConfig> configMap = new HashMap<>();
    private Map<String, String> configKeyValueMap = new HashMap<>();

    private String getConfigCenterUrl() {
        String urlPlaceHolder = "%s";
        if (serverPort != null) {
            urlPlaceHolder = urlPlaceHolder + ":" + serverPort;
        }
        return String.format(urlPlaceHolder + "/config-center/get-app-config", serverUrl);
    }

    /**
     * 初始化上下文，从配置中心获取配置
     */
    public void initContext() {
        List<AppConfig> appConfigs = this.getConfigFromConfigCenter();
        for (AppConfig appConfig : appConfigs) {
            this.configKeyValueMap.put(appConfig.getKey(), appConfig.getValue());
            this.configMap.put(appConfig.getKey(), appConfig);
        }
    }

    /**
     * 从配置中心拉取应用的所有配置
     * @return
     */
    private List<AppConfig> getConfigFromConfigCenter() {
        String configCenterUrl = this.getConfigCenterUrl();

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("appCode", appCode);
        paramMap.put("secretKey", secretKey);

        return RestTemplateUtils.getList(configCenterUrl, paramMap);
    }

    /**
     * 刷新配置
     * @param key
     * @param appConfig
     */
    public synchronized void refreshConfigMap(String key, AppConfig appConfig) {
        configMap.put(key, appConfig);
        configKeyValueMap.put(key, appConfig.getValue());
    }

}
