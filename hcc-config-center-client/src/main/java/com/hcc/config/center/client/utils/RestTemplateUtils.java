package com.hcc.config.center.client.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hcc.config.center.client.entity.AppConfig;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

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

    public static <T> List<T> getList(String url, Map<String, Object> paramMap) {
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class, paramMap);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new IllegalStateException("服务异常！");
        }
        String body = responseEntity.getBody();

        return JsonUtils.toObject(body, new TypeReference<List<T>>() {});
    }

}
