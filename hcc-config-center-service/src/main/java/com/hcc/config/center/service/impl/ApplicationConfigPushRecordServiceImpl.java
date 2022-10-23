package com.hcc.config.center.service.impl;

import com.hcc.config.center.dao.mapper.ApplicationConfigPushRecordMapper;
import com.hcc.config.center.domain.po.ApplicationConfigPushRecordPo;
import com.hcc.config.center.service.ApplicationConfigPushRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ApplicationConfigPushRecordServiceImpl
 *
 * @author hushengjun
 * @date 2022/10/23
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ApplicationConfigPushRecordServiceImpl extends BaseServiceImpl<ApplicationConfigPushRecordMapper, ApplicationConfigPushRecordPo> implements ApplicationConfigPushRecordService {
}
