package com.hcc.config.center.server.controller;

import cn.hutool.core.collection.CollUtil;
import com.hcc.config.center.domain.enums.AppStatusEnum;
import com.hcc.config.center.domain.po.ApplicationConfigPo;
import com.hcc.config.center.domain.po.ApplicationPo;
import com.hcc.config.center.domain.vo.ApplicationConfigVo;
import com.hcc.config.center.domain.vo.ApplicationVo;
import com.hcc.config.center.domain.vo.ServerNodeVo;
import com.hcc.config.center.service.ApplicationConfigService;
import com.hcc.config.center.service.ApplicationService;
import com.hcc.config.center.service.zk.ZkHandler;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("/get-app")
    public ApplicationVo getApp(@RequestParam String appCode,
                                @RequestParam String secretKey) {
        ApplicationPo applicationPo = this.checkApplication(appCode, secretKey);

        ApplicationVo applicationVo = new ApplicationVo();
        BeanUtils.copyProperties(applicationPo, applicationVo);

        return applicationVo;
    }

    @GetMapping("/get-app-config")
    public List<ApplicationConfigVo> getAppConfig(@RequestParam String appCode,
                                                  @RequestParam String secretKey,
                                                  @RequestParam(required = false) Boolean dynamic) {
        ApplicationPo applicationPo = this.checkApplication(appCode, secretKey);

        List<ApplicationConfigPo> applicationConfigPos = applicationConfigService.lambdaQuery()
                .eq(ApplicationConfigPo::getApplicationId, applicationPo.getId())
                .eq(dynamic != null, ApplicationConfigPo::getDynamic, dynamic)
                .list();
        if (CollUtil.isEmpty(applicationConfigPos)) {
            return Collections.emptyList();
        }

        return applicationConfigPos.stream()
                .map(c -> {
                    ApplicationConfigVo applicationConfigVo = new ApplicationConfigVo();
                    BeanUtils.copyProperties(c, applicationConfigVo);

                    return applicationConfigVo;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/get-server-node")
    public List<ServerNodeVo> getServerNodes(@RequestParam String appCode,
                                             @RequestParam String secretKey) {
        this.checkApplication(appCode, secretKey);

        return zkHandler.findAllServerNode();
    }

    private ApplicationPo checkApplication(String appCode, String secretKey) {
        ApplicationPo applicationPo = applicationService.lambdaQuery()
                .eq(ApplicationPo::getAppCode, appCode)
                .eq(ApplicationPo::getSecretKey, secretKey)
                .eq(ApplicationPo::getAppStatus, AppStatusEnum.ONLINE.name())
                .one();
        if (applicationPo == null) {
            throw new IllegalArgumentException("无应用或密钥错误");
        }

        return applicationPo;
    }

}
