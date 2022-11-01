package com.hcc.config.center.client.context;

import com.hcc.config.center.client.entity.AppConfigInfo;
import com.hcc.config.center.client.entity.AppInfo;
import com.hcc.config.center.client.entity.AppMode;
import com.hcc.config.center.client.entity.Constants;
import com.hcc.config.center.client.entity.DynamicConfigRefInfo;
import com.hcc.config.center.client.entity.MsgInfo;
import com.hcc.config.center.client.entity.ServerNodeInfo;
import com.hcc.config.center.client.utils.JsonUtils;
import com.hcc.config.center.client.utils.RestTemplateUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
     * 应用编码
     */
    private String appCode;
    /**
     * 应用密钥
     */
    private String secretKey;
    /**
     * 服务地址，可含端口
     */
    private String serverUrl;
    /**
     * 是否开启动态推送，为false则无法与服务端建立连接，无法获取动态配置变更
     */
    private boolean enableDynamicPush = false;
    /**
     * 长轮询拉取时间间隔，默认5分钟，单位秒
     */
    private Integer pullInterval = 300;
    /**
     * 长轮询轮询hold时间，默认90秒，单位秒
     */
    private Integer longPollingTimeout = 90;

    // 模式：长连接 OR 长轮询，决定客户端以何种方式连接到推送服务器
    private String appMode;
    // 所有配置
    private Map<String, AppConfigInfo> configMap = new HashMap<>();
    // 所有配置，key value形式
    private Map<String, String> configKeyValueMap = new HashMap<>();
    // 服务节点信息，建立长连接使用
    private List<ServerNodeInfo> serverNodeInfos = new ArrayList<>();
    // 动态字段引用信息，使用DynamicValue注解的字段、ListenConfig注解的方法
    private List<DynamicConfigRefInfo> dynamicConfigRefInfos = new ArrayList<>();

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
        // 初始化获取配置中心所有配置
        List<AppConfigInfo> appConfigInfos = this.getConfigFromConfigCenter();
        appConfigInfos.forEach(appConfigInfo -> {
            this.configKeyValueMap.put(appConfigInfo.getKey(), appConfigInfo.getValue());
            this.configMap.put(appConfigInfo.getKey(), appConfigInfo);
        });

        // 获取应用模式
        this.appMode = this.getModeFromConfigCenter();

        // 若模式是长连接则获取服务节点
        if (AppMode.LONG_CONNECT.name().equals(appMode)) {
            this.refreshServerNode();
        }

        // 打印初始化日志
        this.printInitLog();
    }

    /**
     * 打印初始化日志
     */
    private void printInitLog() {
        Map<String, String> staticConfigMap = new HashMap<>();
        Map<String, String> dynamicConfigMap = new HashMap<>();
        configMap.forEach((k, v) -> {
            if (v.getDynamic()) {
                dynamicConfigMap.put(v.getKey(), v.getValue());
            } else {
                staticConfigMap.put(v.getKey(), v.getValue());
            }
        });
        // 打印所有配置信息
        log.info("应用：{}，模式为：{}", appCode, appMode);
        log.info("静态配置：\n{}", JsonUtils.toJsonForBeauty(staticConfigMap));
        log.info("动态配置：\n{}", JsonUtils.toJsonForBeauty(dynamicConfigMap));
        if (AppMode.LONG_CONNECT.name().equals(appMode)) {
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
    private String getModeFromConfigCenter() {
        AppInfo appInfo = RestTemplateUtils.getObject(this.getConfigCenterUrl() + Constants.APP_INFO_URI,
                this.reqParamMap(), AppInfo.class);
        if (appInfo == null || appInfo.getAppMode() == null) {
            throw new IllegalStateException(String.format("未获取到应用：[%s]的模式，请检查应用配置", appCode));
        }

        return appInfo.getAppMode();
    }

    /**
     * 从配置中心拉取应用的所有配置
     * @return
     */
    private List<AppConfigInfo> getConfigFromConfigCenter() {
        String configCenterUrl = this.getConfigCenterUrl() + Constants.APP_CONFIG_URI;

        return RestTemplateUtils.getList(configCenterUrl, this.reqParamMap(), AppConfigInfo.class);
    }

    /**
     * 从配置中心获取动态配置
     * @return
     */
    public List<AppConfigInfo> getDynamicConfigFromConfigCenter() {
        String configCenterUrl = this.getConfigCenterUrl() + Constants.DYNAMIC_APP_CONFIG_URI;

        Map<String, Object> paramMap = this.reqParamMap();

        Map<String, AppConfigInfo> params = new HashMap<>();
        configMap.forEach((k, v) -> {
            if (v.getDynamic()) {
                AppConfigInfo configInfo = new AppConfigInfo();
                configInfo.setKey(k);
                configInfo.setVersion(v.getVersion());

                params.put(k, configInfo);
            }
        });
        dynamicConfigRefInfos.stream()
                // 表示引用了字段，但未在配置中心配置
                .filter(f -> configMap.get(f.getKey()) == null)
                .forEach(fieldInfo -> {
                    AppConfigInfo configInfo = new AppConfigInfo();
                    configInfo.setKey(fieldInfo.getKey());
                    configInfo.setVersion(0);
                    params.putIfAbsent(fieldInfo.getKey(), configInfo);
                });

        paramMap.put("keyParam", JsonUtils.toJson(params.values()));

        return RestTemplateUtils.getList(configCenterUrl, paramMap, AppConfigInfo.class);
    }

    /**
     * 从配置中心获取动态配置
     * @return
     */
    public List<MsgInfo> longPolling() {
        String configCenterUrl = this.getConfigCenterUrl() + Constants.WATCH_URI;

        Map<String, Object> paramMap = this.reqParamMap();
        paramMap.put("timeout", longPollingTimeout);

        Set<String> allKeys = configMap.values().stream()
                .filter(AppConfigInfo::getDynamic)
                .map(AppConfigInfo::getKey)
                .collect(Collectors.toSet());
        allKeys.addAll(
                dynamicConfigRefInfos.stream().map(DynamicConfigRefInfo::getKey).collect(Collectors.toList())
        );
        paramMap.put("keys", String.join(",", allKeys));

        return RestTemplateUtils.getList(configCenterUrl, paramMap, MsgInfo.class);
    }

    /**
     * 获取配置中心服务节点
     * @return
     */
    public void refreshServerNode() {
        String serverNodeUrl = this.getConfigCenterUrl() + Constants.SERVER_NODE_URI;

        List<ServerNodeInfo> serverNodeInfos = RestTemplateUtils.getList(serverNodeUrl, this.reqParamMap(), ServerNodeInfo.class);
        if (!CollectionUtils.isEmpty(serverNodeInfos)) {
            this.serverNodeInfos = serverNodeInfos;
        }
    }

    /**
     * 添加动态字段信息
     * @param dynamicConfigRefInfo
     */
    public synchronized void addDynamicConfigInfo(DynamicConfigRefInfo dynamicConfigRefInfo) {
        dynamicConfigRefInfos.add(dynamicConfigRefInfo);
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
     * 获取配置信息
     * @param key
     * @return
     */
    public AppConfigInfo getConfigInfo(String key) {
        return configMap.get(key);
    }

}
