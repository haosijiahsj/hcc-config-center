package com.hcc.config.center.client.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hcc.config.center.client.entity.AppConfigInfo;
import com.hcc.config.center.client.entity.RestResult;
import com.hcc.config.center.client.entity.ServerNodeInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RestTemplateUtils
 *
 * @author hushengjun
 * @date 2022/10/7
 */
public class RestTemplateUtils {

    private static final RestTemplate restTemplate;

    static {
        restTemplate = new RestTemplate();
    }

    public static String getString(String url, Map<String, Object> paramMap) {
        if (paramMap == null) {
            paramMap = new HashMap<>();
        }
        if (!paramMap.isEmpty()) {
            StringBuilder urlBuilder = new StringBuilder(url + "?");
            for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                urlBuilder.append(String.format("%s={%s}", entry.getKey(), entry.getKey())).append("&");
            }
            url = urlBuilder.toString();
        }
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class, paramMap);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new IllegalStateException("服务异常！");
        }
        return responseEntity.getBody();
    }

    public static List<AppConfigInfo> getAppConfig(String url, Map<String, Object> paramMap) {
        String body = getString(url, paramMap);

        RestResult<List<AppConfigInfo>> result = JsonUtils.toObject(body, new TypeReference<RestResult<List<AppConfigInfo>>>() {
        });
        if (!result.getSuccess()) {
            throw new IllegalStateException("请求配置中心服务失败：" + result.getMessage());
        }

        return result.getData();
    }

    public static List<ServerNodeInfo> getServerNode(String url, Map<String, Object> paramMap) {
        String body = getString(url, paramMap);

        RestResult<List<ServerNodeInfo>> result = JsonUtils.toObject(body, new TypeReference<RestResult<List<ServerNodeInfo>>>() {
        });
        if (!result.getSuccess()) {
            throw new IllegalStateException("请求配置中心服务失败：" + result.getMessage());
        }

        return result.getData();
    }

}
