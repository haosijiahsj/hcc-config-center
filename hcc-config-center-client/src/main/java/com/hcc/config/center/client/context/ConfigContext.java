package com.hcc.config.center.client.context;

import com.hcc.config.center.client.entity.AppConfigInfo;
import com.hcc.config.center.client.entity.AppInfo;
import com.hcc.config.center.client.entity.AppMode;
import com.hcc.config.center.client.entity.Constants;
import com.hcc.config.center.client.entity.DynamicFieldInfo;
import com.hcc.config.center.client.entity.ListenConfigMethodInfo;
import com.hcc.config.center.client.entity.ServerNodeInfo;
import com.hcc.config.center.client.utils.ConvertUtils;
import com.hcc.config.center.client.utils.JsonUtils;
import com.hcc.config.center.client.utils.RestTemplateUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
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
public class ConfigContext {

    /**
     * 基本配置
     */
    private String appCode;
    private String secretKey;
    private String serverUrl;
    private boolean enableDynamicPush = false;
    /**
     * 拉取时间间隔，默认5分钟
     */
    private Integer pullInterval = 300;

    /**
     * 模式：推 OR 拉
     */
    private String appMode;

    private Map<String, AppConfigInfo> configMap = new HashMap<>();
    private Map<String, String> configKeyValueMap = new HashMap<>();
    private List<ServerNodeInfo> serverNodeInfos = new ArrayList<>();
    private List<DynamicFieldInfo> dynamicFieldInfos = new ArrayList<>();
    private List<ListenConfigMethodInfo> listenConfigMethodInfos = new ArrayList<>();

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

        String envServerUrl = System.getenv("CONFIG_CENTER_SERVER_URL");
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
        this.getModeFromConfigCenter();
        if (AppMode.PUSH.name().equals(appMode)) {
            this.refreshServerNode();
        }

        // 打印所有配置信息
        log.info("静态配置：\n{}", JsonUtils.toJsonForBeauty(staticConfigMap));
        log.info("动态配置：\n{}", JsonUtils.toJsonForBeauty(dynamicConfigMap));
        if (AppMode.PUSH.name().equals(appMode)) {
            log.info("服务节点：\n{}", JsonUtils.toJsonForBeauty(this.serverNodeInfos));
        }
    }

    /**
     * 请求参数
     * @return
     */
    private Map<String, Object> reqParamMap() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("appCode", appCode);
        paramMap.put("secretKey", secretKey);

        return paramMap;
    }

    /**
     * 获取应用mode
     */
    private void getModeFromConfigCenter() {
        AppInfo appInfo = RestTemplateUtils.getAppInfo(this.getConfigCenterUrl() + Constants.APP_INFO_URI,
                this.reqParamMap());
        if (appInfo == null) {
            throw new IllegalStateException(String.format("未获取到应用：%s", appCode));
        }

        this.appMode = appInfo.getAppMode();
    }

    /**
     * 从配置中心拉取应用的所有配置
     * @return
     */
    private List<AppConfigInfo> getConfigFromConfigCenter() {
        String configCenterUrl = this.getConfigCenterUrl() + Constants.APP_CONFIG_URI;

        return RestTemplateUtils.getAppConfig(configCenterUrl, this.reqParamMap());
    }

    /**
     * 从配置中心获取动态配置
     * @return
     */
    public List<AppConfigInfo> getDynamicConfigFromConfigCenter() {
        String configCenterUrl = this.getConfigCenterUrl() + Constants.APP_CONFIG_URI;

        Map<String, Object> paramMap = this.reqParamMap();
        paramMap.put("dynamic", true);

        return RestTemplateUtils.getAppConfig(configCenterUrl, paramMap);
    }

    /**
     * 获取配置中心服务节点
     * @return
     */
    public void refreshServerNode() {
        String serverNodeUrl = this.getConfigCenterUrl() + Constants.SERVER_NODE_URI;

        List<ServerNodeInfo> serverNodeInfos = RestTemplateUtils.getServerNode(serverNodeUrl, this.reqParamMap());
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
