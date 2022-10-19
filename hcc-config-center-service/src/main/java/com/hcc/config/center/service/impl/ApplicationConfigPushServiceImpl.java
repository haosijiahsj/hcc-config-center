package com.hcc.config.center.service.impl;

import com.hcc.config.center.domain.po.ApplicationConfigPo;
import com.hcc.config.center.domain.po.ApplicationPo;
import com.hcc.config.center.domain.vo.PushConfigNodeDataVo;
import com.hcc.config.center.service.ApplicationConfigPushService;
import com.hcc.config.center.service.ApplicationConfigService;
import com.hcc.config.center.service.ApplicationService;
import com.hcc.config.center.service.zk.ZkHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * ApplicationConfigPushServiceImpl
 *
 * @author shengjun.hu
 * @date 2022/10/19
 */
@Service
public class ApplicationConfigPushServiceImpl implements ApplicationConfigPushService {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationConfigService applicationConfigService;

    @Autowired
    private ZkHandler zkHandler;

    @Override
    public void pushConfig(Long id) {
        ApplicationConfigPo applicationConfigPo = applicationConfigService.getById(id);
        ApplicationPo applicationPo = applicationService.getById(applicationConfigPo.getApplicationId());

        PushConfigNodeDataVo nodeDataVo = new PushConfigNodeDataVo();
        nodeDataVo.setApplicationConfigId(id);
        nodeDataVo.setAppCode(applicationPo.getAppCode());
        nodeDataVo.setKey(applicationConfigPo.getKey());
        nodeDataVo.setValue(applicationConfigPo.getValue());
        nodeDataVo.setVersion(applicationConfigPo.getVersion());

        zkHandler.addPushConfigNode(nodeDataVo);
    }

}
