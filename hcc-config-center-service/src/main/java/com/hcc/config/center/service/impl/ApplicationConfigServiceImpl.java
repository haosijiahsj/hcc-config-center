package com.hcc.config.center.service.impl;

import com.hcc.config.center.dao.mapper.ApplicationConfigMapper;
import com.hcc.config.center.domain.enums.AppConfigOperateTypeEnum;
import com.hcc.config.center.domain.enums.AppStatusEnum;
import com.hcc.config.center.domain.po.ApplicationConfigHistoryPo;
import com.hcc.config.center.domain.po.ApplicationConfigPo;
import com.hcc.config.center.domain.po.ApplicationPo;
import com.hcc.config.center.service.ApplicationConfigHistoryService;
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

    @Autowired
    private ApplicationConfigHistoryService applicationConfigHistoryService;

    @Override
    public boolean saveOrUpdateConfig(ApplicationConfigPo applicationConfigPo) {
        LocalDateTime now = LocalDateTime.now();
        applicationConfigPo.setUpdateTime(now);

        boolean valueChanged = true;
        ApplicationConfigHistoryPo historyPo = new ApplicationConfigHistoryPo();
        if (applicationConfigPo.getId() == null) {
            ApplicationConfigPo existConfigPo = this.lambdaQuery()
                    .select(ApplicationConfigPo::getId)
                    .eq(ApplicationConfigPo::getApplicationId, applicationConfigPo.getApplicationId())
                    .eq(ApplicationConfigPo::getKey, applicationConfigPo.getKey())
                    .one();
            if (existConfigPo != null) {
                throw new IllegalArgumentException(String.format("key: [%s]已存在", applicationConfigPo.getKey()));
            }

            historyPo.setVersion(1);
            historyPo.setOperateType(AppConfigOperateTypeEnum.CREATE.name());

            applicationConfigPo.setCreateTime(now);
            applicationConfigPo.setVersion(1);

            this.save(applicationConfigPo);
        } else {
            ApplicationConfigPo existConfigPo = this.checkApplicationConfigExist(applicationConfigPo.getId());
            historyPo.setVersion(existConfigPo.getVersion() + 1);
            historyPo.setOperateType(AppConfigOperateTypeEnum.UPDATE.name());

            if (existConfigPo.getValue() == null && applicationConfigPo.getValue() == null) {
                valueChanged = false;
            }
            if (existConfigPo.getValue() != null && existConfigPo.getValue().equals(applicationConfigPo.getValue())) {
                valueChanged = false;
            }

            this.lambdaUpdate()
                    .set(ApplicationConfigPo::getValue, applicationConfigPo.getValue())
                    .set(ApplicationConfigPo::getComment, applicationConfigPo.getComment())
                    .set(ApplicationConfigPo::getUpdateTime, applicationConfigPo.getUpdateTime())
                    .setSql(valueChanged, "version = version + 1")
                    .eq(ApplicationConfigPo::getId, applicationConfigPo.getId())
                    .update();
        }
        historyPo.setApplicationConfigId(applicationConfigPo.getId());
        historyPo.setValue(applicationConfigPo.getValue());
        historyPo.setCreateTime(now);

        // 记录历史
        if (valueChanged) {
            applicationConfigHistoryService.save(historyPo);
        }

        return valueChanged;
    }

    private ApplicationConfigPo checkApplicationConfigExist(Long id) {
        ApplicationConfigPo existApplicationConfigPo = this.getById(id);
        if (existApplicationConfigPo == null) {
            throw new IllegalArgumentException("配置不存在");
        }

        return existApplicationConfigPo;
    }

}
