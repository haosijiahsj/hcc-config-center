package com.hcc.config.center.service.impl;

import com.hcc.config.center.dao.mapper.ApplicationMapper;
import com.hcc.config.center.domain.po.ApplicationPo;
import com.hcc.config.center.service.ApplicationService;
import org.springframework.stereotype.Service;

/**
 * ApplicationServiceImpl
 *
 * @author hushengjun
 * @date 2022/10/6
 */
@Service
public class ApplicationServiceImpl extends BaseServiceImpl<ApplicationMapper, ApplicationPo> implements ApplicationService {
}
