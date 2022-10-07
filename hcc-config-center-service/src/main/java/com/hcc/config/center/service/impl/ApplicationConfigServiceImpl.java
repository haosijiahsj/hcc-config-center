package com.hcc.config.center.service.impl;

import com.hcc.config.center.dao.mapper.ApplicationConfigMapper;
import com.hcc.config.center.domain.po.ApplicationConfigPo;
import com.hcc.config.center.service.ApplicationConfigService;
import org.springframework.stereotype.Service;

/**
 * ApplicationConfigServiceImpl
 *
 * @author hushengjun
 * @date 2022/10/6
 */
@Service
public class ApplicationConfigServiceImpl extends BaseServiceImpl<ApplicationConfigMapper, ApplicationConfigPo> implements ApplicationConfigService {
}
