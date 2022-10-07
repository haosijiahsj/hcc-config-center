package com.hcc.config.center.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hcc.config.center.service.BaseService;

/**
 * BaseServiceImpl
 *
 * @author hushengjun
 * @date 2022/10/6
 */
public class BaseServiceImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> implements BaseService<T> {
}
