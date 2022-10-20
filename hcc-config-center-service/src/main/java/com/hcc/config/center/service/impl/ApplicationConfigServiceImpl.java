package com.hcc.config.center.service.impl;

import com.hcc.config.center.dao.mapper.ApplicationConfigMapper;
import com.hcc.config.center.domain.enums.AppStatusEnum;
import com.hcc.config.center.domain.po.ApplicationConfigPo;
import com.hcc.config.center.domain.po.ApplicationPo;
import com.hcc.config.center.service.ApplicationConfigPushService;
import com.hcc.config.center.service.ApplicationConfigService;
import com.hcc.config.center.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * ApplicationConfigServiceImpl
 *
 * @author hushengjun
 * @date 2022/10/6
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ApplicationConfigServiceImpl extends BaseServiceImpl<ApplicationConfigMapper, ApplicationConfigPo> implements ApplicationConfigService {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationConfigPushService applicationConfigPushService;

    @Override
    public void saveOrUpdateConfig(ApplicationConfigPo applicationConfigPo) {
        LocalDateTime now = LocalDateTime.now();
        applicationConfigPo.setUpdateTime(now);
        if (applicationConfigPo.getId() == null) {
            ApplicationConfigPo existConfigPo = this.lambdaQuery()
                    .eq(ApplicationConfigPo::getApplicationId, applicationConfigPo.getApplicationId())
                    .eq(ApplicationConfigPo::getKey, applicationConfigPo.getKey())
                    .one();
            if (existConfigPo != null) {
                throw new IllegalArgumentException(String.format("key: [%s]已存在", applicationConfigPo.getKey()));
            }

            applicationConfigPo.setCreateTime(now);
            applicationConfigPo.setVersion(1);

            this.save(applicationConfigPo);
        } else {
            this.checkApplicationConfigExist(applicationConfigPo.getId());

            this.lambdaUpdate()
                    .set(ApplicationConfigPo::getValue, applicationConfigPo.getValue())
                    .set(ApplicationConfigPo::getComment, applicationConfigPo.getComment())
                    .setSql("version = version + 1")
                    .eq(ApplicationConfigPo::getId, applicationConfigPo.getId())
                    .update();
        }
        // TODO 记录历史
    }

    private ApplicationConfigPo checkApplicationConfigExist(Long id) {
        ApplicationConfigPo existApplicationConfigPo = this.getById(id);
        if (existApplicationConfigPo == null) {
            throw new IllegalArgumentException("配置不存在");
        }

        return existApplicationConfigPo;
    }

}
