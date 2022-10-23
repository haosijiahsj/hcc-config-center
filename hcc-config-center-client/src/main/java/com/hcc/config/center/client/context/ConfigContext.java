package com.hcc.config.center.client.context;

import com.hcc.config.center.client.constant.Constants;
import com.hcc.config.center.client.entity.AppConfigInfo;
import com.hcc.config.center.client.entity.DynamicFieldInfo;
import com.hcc.config.center.client.entity.ListenConfigMethodInfo;
import com.hcc.config.center.client.entity.ServerNodeInfo;
import com.hcc.config.center.client.utils.ConvertUtils;
import com.hcc.config.center.client.utils.JsonUtils;
import com.hcc.config.center.client.utils.RestTemplateUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置上下文
 *
 * @author hushengjun
 * @date 2022/10/6
 */
@Slf4j
@Data
@NoArgsConstructor
public class ConfigContext {

    private String appCode;
    private String secretKey;
    private String serverUrl;
    private boolean enableDynamicPush = false;

    private Map<String, AppConfigInfo> configMap = new HashMap<>();
    private Map<String, String> configKeyValueMap = new HashMap<>();
    private List<ServerNodeInfo> serverNodeInfos = new ArrayList<>();
    private List<DynamicFieldInfo> dynamicFieldInfos = new ArrayList<>();
    private List<ListenConfigMethodInfo> listenConfigMethodInfos = new ArrayList<>();

    /**
     * 配置中心获取配置地址
     * @return
     */
    public String getConfigCenterGetConfigUrl() {
        return this.getConfigCenterUrl() + Constants.APP_CONFIG_URI;
    }

    /**
     * 配置中心获取服务节点地址
     * @return
     */
    public String getConfigCenterServerNodeUrl() {
        return this.getConfigCenterUrl() + Constants.SERVER_NODE_URI;
    }

    /**
     * 配置中心url
     * @return
     */
    private String getConfigCenterUrl() {
        if (serverUrl != null) {
            return serverUrl;
        }

        String cmdServerUrl = System.getProperty("config.center.serverUrl");
        if (cmdServerUrl != null) {
            return serverUrl;
        }

        String envServerUrl = System.getenv("configCenterServerUrl");
        if (envServerUrl != null) {
            return envServerUrl;
        }

        throw new IllegalStateException("未获取到配置中心服务地址！");
    }

    /**
     * 初始化上下文，从配置中心获取配置
     */
    public void initContext() {
        List<AppConfigInfo> appConfigInfos = this.getConfigFromConfigCenter();

        Map<String, String> staticConfigMap = new HashMap<>();
        Map<String, String> dynamicConfigMap = new HashMap<>();
        appConfigInfos.forEach(appConfigInfo -> {
            this.configKeyValueMap.put(appConfigInfo.getKey(), appConfigInfo.getValue());
            this.configMap.put(appConfigInfo.getKey(), appConfigInfo);
            if (appConfigInfo.getDynamic()) {
                dynamicConfigMap.put(appConfigInfo.getKey(), appConfigInfo.getValue());
            } else {
                staticConfigMap.put(appConfigInfo.getKey(), appConfigInfo.getValue());
            }
        });
        this.refreshServerNode();

        // 打印所有配置信息
        log.info("静态配置：\n{}", JsonUtils.toJsonForBeauty(staticConfigMap));
        log.info("动态配置：\n{}", JsonUtils.toJsonForBeauty(dynamicConfigMap));
        log.info("服务节点：\n{}", JsonUtils.toJsonForBeauty(this.serverNodeInfos));
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

    /**
     * 获取配置中心服务节点
     * @return
     */
    public void refreshServerNode() {
        String serverNodeUrl = this.getConfigCenterServerNodeUrl();

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("appCode", appCode);
        paramMap.put("secretKey", secretKey);
        List<ServerNodeInfo> serverNodeInfos = RestTemplateUtils.getServerNode(serverNodeUrl, paramMap);
        if (!CollectionUtils.isEmpty(serverNodeInfos)) {
            this.serverNodeInfos = serverNodeInfos;
        }
    }

    /**
     * 添加动态字段信息
     * @param dynamicFieldInfo
     */
    public synchronized void addDynamicFieldInfo(DynamicFieldInfo dynamicFieldInfo) {
        dynamicFieldInfos.add(dynamicFieldInfo);
    }

    /**
     * 添加ListenConfig方法信息
     * @param methodInfo
     */
    public synchronized void addListenConfigMethodInfo(ListenConfigMethodInfo methodInfo) {
        listenConfigMethodInfos.add(methodInfo);
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
    public Integer getConfigVersion(String key) {
        AppConfigInfo appConfigInfo = configMap.get(key);
        if (appConfigInfo == null) {
            return null;
        }

        return appConfigInfo.getVersion();
    }

    /**
     * 获取配置值
     * @param key
     * @return
     */
    public String getConfigValue(String key) {
        return configKeyValueMap.get(key);
    }

    /**
     * 获取配置值并转换为目标类型
     * @param key
     * @param targetClass
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getConfigValue(String key, Class<T> targetClass) {
        String value = configKeyValueMap.get(key);
        if (value == null) {
            return null;
        }

        return (T) ConvertUtils.convertValueToTargetType(value, targetClass);
    }

    /**
     * 获取配置信息
     * @param key
     * @return
     */
    public AppConfigInfo getConfigInfo(String key) {
        return configMap.get(key);
    }

}
