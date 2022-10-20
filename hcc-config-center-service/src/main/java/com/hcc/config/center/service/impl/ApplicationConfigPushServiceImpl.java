package com.hcc.config.center.service.impl;

import com.hcc.config.center.domain.enums.AppStatusEnum;
import com.hcc.config.center.domain.po.ApplicationConfigPo;
import com.hcc.config.center.domain.po.ApplicationPo;
import com.hcc.config.center.domain.vo.PushConfigNodeDataVo;
import com.hcc.config.center.service.ApplicationConfigPushService;
import com.hcc.config.center.service.ApplicationConfigService;
import com.hcc.config.center.service.ApplicationService;
import com.hcc.config.center.service.zk.ZkHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private ZkHandler zkHandler;

    @Override
    public void pushConfig(Long id, Boolean forceUpdate) {
        ApplicationConfigPo applicationConfigPo = applicationConfigService.getById(id);
        ApplicationPo applicationPo = applicationService.getById(applicationConfigPo.getApplicationId());

        PushConfigNodeDataVo nodeDataVo = new PushConfigNodeDataVo();
        nodeDataVo.setApplicationConfigId(id);
        nodeDataVo.setAppCode(applicationPo.getAppCode());
        nodeDataVo.setKey(applicationConfigPo.getKey());
        nodeDataVo.setValue(applicationConfigPo.getValue());
        nodeDataVo.setVersion(applicationConfigPo.getVersion());
        nodeDataVo.setForceUpdate(forceUpdate);
        nodeDataVo.setForceUpdate(true);

        zkHandler.addPushConfigNode(nodeDataVo);

        // TODO 记录推送记录
    }

    @Override
    public void pushDeletedConfig(Long id) {
        ApplicationConfigPo applicationConfigPo = this.checkApplicationConfigExist(id);

        ApplicationPo applicationPo = applicationService.getById(applicationConfigPo.getApplicationId());
        applicationConfigService.removeById(id);

        if (applicationConfigPo.getDynamic() && AppStatusEnum.ONLINE.name().equals(applicationPo.getAppStatus())) {
            PushConfigNodeDataVo nodeDataVo = new PushConfigNodeDataVo();
            nodeDataVo.setAppCode(applicationPo.getAppCode());
            nodeDataVo.setKey(applicationConfigPo.getKey());
            nodeDataVo.setForceUpdate(true);

            zkHandler.deletePushConfigNode(nodeDataVo);
        }

        // TODO 记录推送记录
    }

    private ApplicationConfigPo checkApplicationConfigExist(Long id) {
        ApplicationConfigPo existApplicationConfigPo = applicationConfigService.getById(id);
        if (existApplicationConfigPo == null) {
            throw new IllegalArgumentException("配置不存在");
        }

        return existApplicationConfigPo;
    }

}
