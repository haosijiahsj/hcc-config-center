package com.hcc.config.center.client.entity;

import lombok.Data;

/**
 * RefreshValueDefine
 *
 * @author shengjun.hu
 * @date 2022/10/8
 */
@Data
public class RefreshValueDefine {

    private String key;
    private String beanName;
    private Class<?> beanClass;

}
