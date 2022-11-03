package com.hcc.config.center.service.impl;

import com.hcc.config.center.domain.enums.AppConfigOperateTypeEnum;
import com.hcc.config.center.domain.enums.AppStatusEnum;
import com.hcc.config.center.domain.enums.PushConfigMsgType;
import com.hcc.config.center.domain.po.ApplicationConfigHistoryPo;
import com.hcc.config.center.domain.po.ApplicationConfigPo;
import com.hcc.config.center.domain.po.ApplicationConfigPushRecordPo;
import com.hcc.config.center.domain.po.ApplicationPo;
import com.hcc.config.center.domain.vo.PushConfigNodeDataVo;
import com.hcc.config.center.service.ApplicationConfigHistoryService;
import com.hcc.config.center.service.ApplicationConfigPushRecordService;
import com.hcc.config.center.service.ApplicationConfigPushService;
import com.hcc.config.center.service.ApplicationConfigService;
import com.hcc.config.center.service.ApplicationService;
import com.hcc.config.center.service.zk.ZkHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * ApplicationConfigPushServiceImpl
 *
 * @author shengjun.hu
 * @date 2022/10/19
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ApplicationConfigPushServiceImpl implements ApplicationConfigPushService {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationConfigService applicationConfigService;

    @Autowired
    private ApplicationConfigHistoryService applicationConfigHistoryService;

    @Autowired
    private ApplicationConfigPushRecordService applicationConfigPushRecordService;

    @Autowired
    private ZkHandler zkHandler;

    @Override
    public void pushConfig(Long id, Boolean forceUpdate) {
        ApplicationConfigPo applicationConfigPo = applicationConfigService.getById(id);
        ApplicationPo applicationPo = applicationService.getById(applicationConfigPo.getApplicationId());

        PushConfigNodeDataVo nodeDataVo = new PushConfigNodeDataVo();
        nodeDataVo.setAppCode(applicationPo.getAppCode());
        nodeDataVo.setAppMode(applicationPo.getAppMode());
        nodeDataVo.setKey(applicationConfigPo.getKey());
        nodeDataVo.setValue(applicationConfigPo.getValue());
        nodeDataVo.setVersion(applicationConfigPo.getVersion());
        nodeDataVo.setForceUpdate(forceUpdate != null ? forceUpdate : false);

        zkHandler.addPushConfigNode(nodeDataVo);

        ApplicationConfigPushRecordPo pushRecordPo = new ApplicationConfigPushRecordPo();
        pushRecordPo.setApplicationConfigId(id);
        pushRecordPo.setValue(applicationConfigPo.getValue());
        pushRecordPo.setVersion(applicationConfigPo.getVersion());
        pushRecordPo.setCreateTime(LocalDateTime.now());
        applicationConfigPushRecordService.save(pushRecordPo);
    }

    @Override
    public void pushDeletedConfig(Long id) {
        ApplicationConfigPo applicationConfigPo = this.checkApplicationConfigExist(id);

        ApplicationPo applicationPo = applicationService.getById(applicationConfigPo.getApplicationId());
        applicationConfigService.removeById(id);

        ApplicationConfigHistoryPo historyPo = new ApplicationConfigHistoryPo();
        historyPo.setApplicationConfigId(id);
        historyPo.setValue(applicationConfigPo.getValue());
        historyPo.setVersion(applicationConfigPo.getVersion());
        historyPo.setOperateType(AppConfigOperateTypeEnum.DELETE.name());
        historyPo.setCreateTime(LocalDateTime.now());
        applicationConfigHistoryService.save(historyPo);

        if (applicationConfigPo.getDynamic() && AppStatusEnum.ONLINE.name().equals(applicationPo.getAppStatus())) {
            PushConfigNodeDataVo nodeDataVo = new PushConfigNodeDataVo();
            nodeDataVo.setMsgType(PushConfigMsgType.CONFIG_DELETE.name());
            nodeDataVo.setAppCode(applicationPo.getAppCode());
            nodeDataVo.setKey(applicationConfigPo.getKey());
            nodeDataVo.setValue(null);
            // 删除的版本设置为0，并设置强制推送
            nodeDataVo.setVersion(0);
            nodeDataVo.setForceUpdate(true);

            zkHandler.addPushConfigNode(nodeDataVo);

            ApplicationConfigPushRecordPo pushRecordPo = new ApplicationConfigPushRecordPo();
            pushRecordPo.setApplicationConfigId(id);
            pushRecordPo.setValue(null);
            pushRecordPo.setVersion(0);
            pushRecordPo.setCreateTime(LocalDateTime.now());
            applicationConfigPushRecordService.save(pushRecordPo);
        }
    }

    private ApplicationConfigPo checkApplicationConfigExist(Long id) {
        ApplicationConfigPo existApplicationConfigPo = applicationConfigService.getById(id);
        if (existApplicationConfigPo == null) {
            throw new IllegalArgumentException("配置不存在");
        }

        return existApplicationConfigPo;
    }

}
