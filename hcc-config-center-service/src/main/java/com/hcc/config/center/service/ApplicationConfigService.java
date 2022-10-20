package com.hcc.config.center.service;

import com.hcc.config.center.domain.po.ApplicationConfigPo;

/**
 * ApplicationConfigService
 *
 * @author hushengjun
 * @date 2022/10/6
 */
public interface ApplicationConfigService extends BaseService<ApplicationConfigPo> {

    /**
     * 保存或更新配置
     * @param applicationConfigPo
     */
    void saveOrUpdateConfig(ApplicationConfigPo applicationConfigPo);

}
