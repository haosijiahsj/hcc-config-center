package com.hcc.config.center.client.utils;

import org.springframework.web.client.RestTemplate;

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

}
