package com.hcc.config.center.web.controller;

import com.hcc.config.center.domain.po.ApplicationConfigPo;
import com.hcc.config.center.domain.po.ApplicationPo;
import com.hcc.config.center.service.ApplicationConfigService;
import com.hcc.config.center.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ApplicationConfigController
 *
 * @author hushengjun
 * @date 2022/10/6
 */
@RestController
@RequestMapping("/application-config")
public class ApplicationConfigController {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationConfigService applicationConfigService;

    @GetMapping("/get-config")
    public List<ApplicationConfigPo> getAppConfig(String appCode, String secretKey) {
        ApplicationPo applicationPo = applicationService.lambdaQuery()
                .eq(ApplicationPo::getAppCode, appCode)
                .eq(ApplicationPo::getSecretKey, secretKey)
                .one();
        if (applicationPo == null) {
            throw new IllegalArgumentException("无应用密钥错误");
        }

        return applicationConfigService.lambdaQuery()
                .eq(ApplicationConfigPo::getApplicationId, applicationPo.getId())
                .list();
    }

}
