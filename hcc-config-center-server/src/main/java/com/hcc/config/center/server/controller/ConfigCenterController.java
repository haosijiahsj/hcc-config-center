package com.hcc.config.center.server.controller;

import cn.hutool.core.collection.CollUtil;
import com.hcc.config.center.domain.enums.AppStatusEnum;
import com.hcc.config.center.domain.po.ApplicationConfigPo;
import com.hcc.config.center.domain.po.ApplicationPo;
import com.hcc.config.center.domain.vo.AppConfigInfo;
import com.hcc.config.center.domain.vo.ApplicationVo;
import com.hcc.config.center.domain.vo.ServerNodeVo;
import com.hcc.config.center.server.context.LongPollingContext;
import com.hcc.config.center.service.ApplicationConfigService;
import com.hcc.config.center.service.ApplicationService;
import com.hcc.config.center.service.utils.JsonUtils;
import com.hcc.config.center.service.zk.ZkHandler;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 对client提供的接口
 *
 * @author shengjun.hu
 * @date 2022/10/8
 */
@Slf4j
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
    public List<AppConfigInfo> getAppConfig(@RequestParam String appCode,
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
                    AppConfigInfo appConfigInfo = new AppConfigInfo();
                    BeanUtils.copyProperties(c, appConfigInfo);

                    return appConfigInfo;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/get-dynamic-app-config")
    public List<AppConfigInfo> getAppConfigForDynamic(@RequestParam String appCode,
                                                      @RequestParam String secretKey,
                                                      @RequestParam String keyParam) {
        ApplicationPo applicationPo = this.checkApplication(appCode, secretKey);
        List<AppConfigInfo> configParams = JsonUtils.toList(keyParam, AppConfigInfo.class);

        List<ApplicationConfigPo> applicationConfigPos = applicationConfigService.lambdaQuery()
                .eq(ApplicationConfigPo::getApplicationId, applicationPo.getId())
                .eq(ApplicationConfigPo::getDynamic, true)
                .eq(ApplicationConfigPo::getKey, configParams.stream().map(AppConfigInfo::getKey).collect(Collectors.toSet()))
                .list();
        if (CollUtil.isEmpty(applicationConfigPos)) {
            return Collections.emptyList();
        }

        Map<String, ApplicationConfigPo> keyApplicationConfigMap = applicationConfigPos.stream()
                .collect(Collectors.toMap(ApplicationConfigPo::getKey, Function.identity()));

        List<AppConfigInfo> changeConfigs = new ArrayList<>();
        for (AppConfigInfo configParam : configParams) {
            ApplicationConfigPo applicationConfigPo = keyApplicationConfigMap.get(configParam.getKey());
            AppConfigInfo appConfigInfo = new AppConfigInfo();
            appConfigInfo.setAppCode(appCode);
            appConfigInfo.setDynamic(true);
            appConfigInfo.setKey(configParam.getKey());

            if (applicationConfigPo == null) {
                // 删除了
                appConfigInfo.setVersion(0);
            } else {
                if (configParam.getVersion() >= applicationConfigPo.getVersion()) {
                    continue;
                }
                // 变化了
                appConfigInfo.setValue(applicationConfigPo.getValue());
                appConfigInfo.setVersion(applicationConfigPo.getVersion());
            }
            changeConfigs.add(appConfigInfo);
        }

        return changeConfigs;
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

    @GetMapping("/watch")
    public DeferredResult<List<AppConfigInfo>> watchAppCode(@RequestParam String appCode,
                                                            @RequestParam String secretKey,
                                                            @RequestParam Long timeout,
                                                            @RequestParam List<String> keys) {
        DeferredResult<List<AppConfigInfo>> result = new DeferredResult<>(timeout, Collections.emptyList());
        this.checkApplication(appCode, secretKey);

        String clientId = appCode + "_" + UUID.randomUUID().toString().replaceAll("-", "");
        LongPollingContext.add(clientId, appCode, result, keys);

        result.onTimeout(() -> {
            log.info("clientId: [{}], 超时返回", clientId);
            LongPollingContext.remove(clientId);
        });
        result.onCompletion(() -> {
            log.info("clientId: [{}], 成功返回", clientId);
            LongPollingContext.remove(clientId);
        });
        result.onError(e -> {
            log.error(String.format("clientId: [%s], 异常返回", clientId), e);
            LongPollingContext.remove(clientId);
        });

        return result;
    }

}
