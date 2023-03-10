package com.hcc.config.center.client.context;

import com.hcc.config.center.client.ConfigChangeHandler;
import com.hcc.config.center.client.entity.AppConfigInfo;
import com.hcc.config.center.client.entity.AppInfo;
import com.hcc.config.center.client.entity.AppMode;
import com.hcc.config.center.client.entity.Constants;
import com.hcc.config.center.client.entity.ReceivedServerMsg;
import com.hcc.config.center.client.entity.RefreshConfigRefInfo;
import com.hcc.config.center.client.entity.ServerNodeInfo;
import com.hcc.config.center.client.utils.CollUtils;
import com.hcc.config.center.client.utils.JsonUtils;
import com.hcc.config.center.client.utils.RestTemplateUtils;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
     * 是否检查配置在配置中心存在，为true则检查不存在抛出异常
     */
    private boolean checkConfigExist = true;
    /**
     * 长轮询拉取时间间隔，默认5分钟，单位秒
     */
    private Integer pullInterval = 300;
    /**
     * 长轮询轮询hold时间，默认90秒，单位秒
     */
    private Integer longPollingTimeout = 90;

    // 模式：长连接 OR 长轮询，决定客户端以何种方式连接到推送服务器
    @Setter(AccessLevel.NONE)
    private String appMode;
    // 所有配置
    @Setter(AccessLevel.NONE)
    private Map<String, AppConfigInfo> configMap = new HashMap<>();
    // 所有配置，key value形式
    @Setter(AccessLevel.NONE)
    private Map<String, String> configKeyValueMap = new HashMap<>();
    // 服务节点信息，建立长连接使用
    @Setter(AccessLevel.NONE)
    private List<ServerNodeInfo> serverNodeInfos = new ArrayList<>();
    // 动态字段引用信息，使用ConfigValue(refresh = true)注解的字段、ListenConfig注解的方法
    @Setter(AccessLevel.NONE)
    private List<RefreshConfigRefInfo> refreshConfigRefInfos = new ArrayList<>();
    // 配置变更处理器
    @Setter(AccessLevel.NONE)
    private List<ConfigChangeHandler> configChangeHandlers = new ArrayList<>();

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
        List<AppConfigInfo> appConfigInfos = this.getConfigFromConfigCenter(Collections.emptyList());
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
        // 打印所有配置信息
        log.info("应用：{}，模式为：{}", appCode, appMode);
        log.info("配置信息：\n{}", JsonUtils.toJsonForBeauty(configKeyValueMap));
        if (AppMode.LONG_CONNECT.name().equals(appMode)) {
            log.info("服务节点：\n{}", JsonUtils.toJsonForBeauty(this.serverNodeInfos));
        }
    }

    /**
     * 请求参数
     * @return
     */
    private Map<String, Object> baseReqParamMap() {
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
                this.baseReqParamMap(), AppInfo.class);
        if (appInfo == null || appInfo.getAppMode() == null) {
            throw new IllegalStateException(String.format("未获取到应用：[%s]的模式，请检查应用配置", appCode));
        }

        return appInfo.getAppMode();
    }

    /**
     * 从配置中心拉取应用的指定keys的配置
     * @param keys 为空则获取所有配置
     * @return
     */
    public List<AppConfigInfo> getConfigFromConfigCenter(List<String> keys) {
        String configCenterUrl = this.getConfigCenterUrl() + Constants.APP_CONFIG_URI;
        Map<String, Object> paramMap = this.baseReqParamMap();
        if (keys != null && !keys.isEmpty()) {
            paramMap.put("keys", keys);
        }

        return RestTemplateUtils.getList(configCenterUrl, paramMap, AppConfigInfo.class);
    }

    /**
     * 从配置中心获取变更了的配置
     * @return
     */
    public List<ReceivedServerMsg> getChangedConfigFromConfigCenter() {
        String configCenterUrl = this.getConfigCenterUrl() + Constants.CHANGED_APP_CONFIG_URI;

        Map<String, Object> paramMap = this.baseReqParamMap();

        Map<String, AppConfigInfo> params = new HashMap<>();
        configMap.forEach((k, v) -> {
            AppConfigInfo configInfo = new AppConfigInfo();
            configInfo.setKey(k);
            configInfo.setVersion(v.getVersion());

            params.put(k, configInfo);
        });
        refreshConfigRefInfos.stream()
                // 表示引用了字段，但未在配置中心配置
                .filter(f -> configMap.get(f.getKey()) == null)
                .forEach(fieldInfo -> {
                    AppConfigInfo configInfo = new AppConfigInfo();
                    configInfo.setKey(fieldInfo.getKey());
                    configInfo.setVersion(0);
                    params.putIfAbsent(fieldInfo.getKey(), configInfo);
                });
        for (ConfigChangeHandler handler : configChangeHandlers) {
            List<String> keys = handler.keys();
            for (String key : keys) {
                if (configMap.get(key) != null) {
                    continue;
                }
                AppConfigInfo configInfo = new AppConfigInfo();
                configInfo.setKey(key);
                configInfo.setVersion(0);
                params.putIfAbsent(key, configInfo);
            }
        }

        paramMap.put("keyParam", JsonUtils.toJson(params.values()));

        return RestTemplateUtils.getList(configCenterUrl, paramMap, ReceivedServerMsg.class);
    }

    /**
     * 从配置中心获取动态配置
     * @return
     */
    public List<ReceivedServerMsg> longPolling() {
        String configCenterUrl = this.getConfigCenterUrl() + Constants.WATCH_URI;

        Map<String, Object> paramMap = this.baseReqParamMap();
        paramMap.put("timeout", longPollingTimeout);

        Set<String> allKeys = configMap.values().stream()
                .map(AppConfigInfo::getKey)
                .collect(Collectors.toSet());
        allKeys.addAll(
                refreshConfigRefInfos.stream().map(RefreshConfigRefInfo::getKey).collect(Collectors.toList())
        );
        allKeys.addAll(
                configChangeHandlers.stream().map(ConfigChangeHandler::keys).flatMap(Collection::stream).collect(Collectors.toList())
        );
        paramMap.put("keys", String.join(",", allKeys));

        return RestTemplateUtils.getList(configCenterUrl, paramMap, ReceivedServerMsg.class);
    }

    /**
     * 获取配置中心服务节点
     * @return
     */
    public void refreshServerNode() {
        String serverNodeUrl = this.getConfigCenterUrl() + Constants.SERVER_NODE_URI;

        List<ServerNodeInfo> serverNodeInfos = RestTemplateUtils.getList(serverNodeUrl, this.baseReqParamMap(), ServerNodeInfo.class);
        if (CollUtils.isNotEmpty(serverNodeInfos)) {
            this.serverNodeInfos = serverNodeInfos;
        }
    }

    /**
     * 添加动态字段信息
     * @param refreshConfigRefInfo
     */
    public synchronized void addRefreshConfigRefInfo(RefreshConfigRefInfo refreshConfigRefInfo) {
        refreshConfigRefInfos.add(refreshConfigRefInfo);
    }

    /**
     * 批量添加处理器
     * @param handlers
     */
    public synchronized void addConfigChangeHandlers(List<ConfigChangeHandler> handlers) {
        if (CollUtils.isEmpty(handlers)) {
            return;
        }
        handlers.forEach(handler -> {
            if (CollUtils.isEmpty(handler.keys())) {
                throw new IllegalArgumentException(String.format("ConfigChangeHandler: [%s], 监听的key不能为空！", handler.getClass()));
            }
        });
        configChangeHandlers.addAll(handlers);
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
     * 获取配置信息
     * @param key
     * @return
     */
    public AppConfigInfo getConfigInfo(String key) {
        return configMap.get(key);
    }

}
