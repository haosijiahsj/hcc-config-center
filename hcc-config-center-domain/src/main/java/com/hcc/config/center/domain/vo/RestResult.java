package com.hcc.config.center.domain.vo;

import lombok.Data;

/**
 * RestResult
 *
 * @author hushengjun
 * @date 2022/10/6
 */
@Data
public class RestResult<T> {

    private Boolean success;
    private Integer code;
    private String message;
    private T data;

}
