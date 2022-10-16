package com.hcc.config.center.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hcc.config.center.domain.param.PageParam;
import com.hcc.config.center.domain.result.PageResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * BaseService
 *
 * @author hushengjun
 * @date 2022/10/6
 */
public interface BaseService<T> extends IService<T> {

    String limitSql = "LIMIT %s, %s";

    /**
     * 分页查询
     * @param currentPage
     * @param pageSize
     * @param wrapper
     * @return
     */
    @Transactional(readOnly = true)
    default PageResult<T> page(long currentPage, long pageSize, Wrapper<T> wrapper) {
        Page<T> page = new Page<>();
        page.setCurrent(currentPage);
        page.setSize(pageSize);

        PageResult<T> pageResult = new PageResult<>();
        Page<T> result = this.page(page, wrapper);
        if (result == null) {
            // 应该不会为空
            pageResult.setPage(currentPage);
            pageResult.setSize(pageSize);
            pageResult.setTotalPage(0L);
            pageResult.setTotalSize(0L);
            pageResult.setData(Collections.emptyList());

            return pageResult;
        }

        pageResult.setPage(page.getCurrent());
        pageResult.setSize(page.getSize());
        pageResult.setTotalPage(page.getPages());
        pageResult.setTotalSize(page.getTotal());
        pageResult.setData(result.getRecords());

        return pageResult;
    }

    /**
     * 分页查询
     * @param pageParam
     * @param wrapper
     * @return
     */
    @Transactional(readOnly = true)
    default PageResult<T> page(PageParam pageParam, Wrapper<T> wrapper) {
        return this.page(pageParam.getPage(), pageParam.getSize(), wrapper);
    }

    /**
     * 分页查询不带count
     * @param pageParam
     * @param wrapper
     * @return
     */
    @Transactional(readOnly = true)
    default List<T> pageWithoutCount(PageParam pageParam, Wrapper<T> wrapper) {
        return this.pageWithoutCount(pageParam.getPage(), pageParam.getSize(), wrapper);
    }

    /**
     * 分页查询不带count
     * @param currentPage
     * @param pageSize
     * @param wrapper
     * @return
     */
    @Transactional(readOnly = true)
    default List<T> pageWithoutCount(long currentPage, long pageSize, Wrapper<T> wrapper) {
        String lastSql = String.format(limitSql, (currentPage - 1) * pageSize, pageSize);
        if (wrapper instanceof LambdaQueryWrapper) {
            ((LambdaQueryWrapper<T>) wrapper).last(lastSql);
        } else if (wrapper instanceof QueryWrapper) {
            ((QueryWrapper<T>) wrapper).last(lastSql);
        } else {
            throw new IllegalArgumentException("不支持的wrapper！");
        }

        return this.list(wrapper);
    }

    /**
     * 保存并返回影响行数
     * @param entity
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    default int saveForRows(T entity) {
        return this.getBaseMapper().insert(entity);
    }

    /**
     * 更新并返回影响行数
     * @param entity
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    default int updateByIdForRows(T entity) {
        return this.getBaseMapper().updateById(entity);
    }

    /**
     * 更新并返回影响行数
     * @param entity
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    default int updateByIdForRows(T entity, Wrapper<T> wrapper) {
        return this.getBaseMapper().update(entity, wrapper);
    }

}
