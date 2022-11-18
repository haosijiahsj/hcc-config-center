package com.hcc.config.center.client.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hcc.config.center.client.entity.RestResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
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
        if (responseEntity.getStatusCode() != HttpStatus.OK && responseEntity.getStatusCode() != HttpStatus.NOT_MODIFIED) {
            throw new IllegalStateException("服务异常！");
        }
        return responseEntity.getBody();
    }

    public static <T> List<T> getList(String url, Map<String, Object> paramMap, Class<T> targetClass) {
        String body = getString(url, paramMap);

        RestResult<Object> result = JsonUtils.toObject(body, new TypeReference<RestResult<Object>>() {
        });
        if (!result.getSuccess()) {
            throw new IllegalStateException("url：" + result.getMessage());
        }

        if (result.getData() == null) {
            return Collections.emptyList();
        }

        String data = JsonUtils.toJson(result.getData());

        return JsonUtils.toList(data, targetClass);
    }

    public static <T> T getObject(String url, Map<String, Object> paramMap, Class<T> targetClass) {
        String body = getString(url, paramMap);

        RestResult<Object> result = JsonUtils.toObject(body, new TypeReference<RestResult<Object>>() {
        });
        if (!result.getSuccess()) {
            throw new IllegalStateException("url：" + result.getMessage());
        }
        if (result.getData() == null) {
            return null;
        }

        String data = JsonUtils.toJson(result.getData());

        return JsonUtils.toObject(data, targetClass);
    }

}
