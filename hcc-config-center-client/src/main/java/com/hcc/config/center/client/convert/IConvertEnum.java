package com.hcc.config.center.client.convert;

import java.io.Serializable;

/**
 * 自定义的枚举转换
 *
 * @author shengjun.hu
 * @date 2022/11/22
 */
public interface IConvertEnum<T extends Serializable> {

    /**
     * 泛型尽量使用String、Long、Integer等
     * @return
     */
    T getValue();

}
