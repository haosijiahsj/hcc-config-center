package com.hcc.config.center.client.convert;

/**
 * 转对象时，对象需要实现该接口（作为一个标记）<br/>
 * 必须提供一个公开的无参构造器
 *
 * @author shengjun.hu
 * @date 2022/11/22
 */
public interface IConvertObject {

    default SourceType sourceType() {
        return SourceType.JSON;
    }

    enum SourceType {
        JSON
    }

}
