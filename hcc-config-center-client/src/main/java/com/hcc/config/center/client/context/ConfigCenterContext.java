package com.hcc.config.center.client.context;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hcc.config.center.client.entity.AppConfig;
import com.hcc.config.center.client.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

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

    private List<AppConfig> appConfigs;
    private Map<String, AppConfig> configMap = new HashMap<>();
    private Map<String, Object> configKeyValueMap = new HashMap<>();

    private String getConfigCenterUrl() {
        return String.format("%s/config-center/application-config/get-config", serverUrl);
    }

    /**
     * 初始化上下文，从配置中心获取配置
     */
    public void initContext() {
        this.appConfigs = this.getConfigFromRemote();
        for (AppConfig appConfig : appConfigs) {
            this.configKeyValueMap.put(appConfig.getKey(), appConfig.getValue());
            this.configMap.put(appConfig.getKey(), appConfig);
        }
    }

    private List<AppConfig> getConfigFromRemote() {
        String configCenterUrl = this.getConfigCenterUrl();
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("appCode", appCode);
        paramMap.put("secretKey", secretKey);
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(configCenterUrl, String.class, paramMap);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new IllegalStateException("配置中心服务异常！");
        }
        String body = responseEntity.getBody();
        List<AppConfig> appConfigs = JsonUtils.toObject(body, new TypeReference<List<AppConfig>>() {
        });

        return appConfigs;
    }

}
