package com.hcc.config.center.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * PageResult
 *
 * @author hushengjun
 * @date 2022/10/6
 */
@Data
public class PageResult<T> {

    private Long page;
    private Long size;
    private Long totalPage;
    private Long totalSize;
    private List<T> data;

}
