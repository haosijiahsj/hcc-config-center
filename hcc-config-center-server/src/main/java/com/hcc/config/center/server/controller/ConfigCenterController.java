package com.hcc.config.center.server.controller;

import com.hcc.config.center.domain.enums.AppStatusEnum;
import com.hcc.config.center.domain.po.ApplicationConfigPo;
import com.hcc.config.center.domain.po.ApplicationPo;
import com.hcc.config.center.domain.vo.ServerNodeVo;
import com.hcc.config.center.service.ApplicationConfigService;
import com.hcc.config.center.service.ApplicationService;
import com.hcc.config.center.service.zk.ZkHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 对client提供的接口
 *
 * @author shengjun.hu
 * @date 2022/10/8
 */
@RestController
@RequestMapping
public class ConfigCenterController {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationConfigService applicationConfigService;

    @Autowired
    private ZkHandler zkHandler;

    @GetMapping("/get-app-config")
    public List<ApplicationConfigPo> getAppConfig(@RequestParam String appCode, @RequestParam String secretKey) {
        ApplicationPo applicationPo = applicationService.lambdaQuery()
                .eq(ApplicationPo::getAppCode, appCode)
                .eq(ApplicationPo::getSecretKey, secretKey)
                .eq(ApplicationPo::getAppStatus, AppStatusEnum.ONLINE.name())
                .one();
        if (applicationPo == null) {
            throw new IllegalArgumentException("无应用或密钥错误");
        }

        return applicationConfigService.lambdaQuery()
                .eq(ApplicationConfigPo::getApplicationId, applicationPo.getId())
                .list();
    }

    @GetMapping("/get-server-node")
    public List<ServerNodeVo> getServerNodes() {
        return zkHandler.findAllServerNode();
    }

}
