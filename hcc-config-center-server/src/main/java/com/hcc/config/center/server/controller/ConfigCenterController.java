package com.hcc.config.center.server.controller;

import cn.hutool.core.collection.CollUtil;
import com.hcc.config.center.domain.enums.AppStatusEnum;
import com.hcc.config.center.domain.enums.PushConfigMsgType;
import com.hcc.config.center.domain.po.ApplicationConfigPo;
import com.hcc.config.center.domain.po.ApplicationPo;
import com.hcc.config.center.domain.vo.AppConfigInfo;
import com.hcc.config.center.domain.vo.ApplicationVo;
import com.hcc.config.center.domain.vo.PushConfigClientMsgVo;
import com.hcc.config.center.domain.vo.ServerNodeVo;
import com.hcc.config.center.service.ApplicationConfigService;
import com.hcc.config.center.service.ApplicationService;
import com.hcc.config.center.service.context.LongPollingContext;
import com.hcc.config.center.service.utils.JsonUtils;
import com.hcc.config.center.service.zk.ZkHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletResponse;
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
                                            @RequestParam(required = false) List<String> keys) {
        ApplicationPo applicationPo = this.checkApplication(appCode, secretKey);

        List<ApplicationConfigPo> applicationConfigPos = applicationConfigService.lambdaQuery()
                .eq(ApplicationConfigPo::getApplicationId, applicationPo.getId())
                .in(CollUtil.isNotEmpty(keys), ApplicationConfigPo::getKey, keys)
                .list();
        if (CollUtil.isEmpty(applicationConfigPos)) {
            return Collections.emptyList();
        }

        return applicationConfigPos.stream()
                .map(c -> {
                    AppConfigInfo appConfigInfo = new AppConfigInfo();
                    BeanUtils.copyProperties(c, appConfigInfo);
                    appConfigInfo.setAppCode(applicationPo.getAppCode());

                    return appConfigInfo;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/get-changed-app-config")
    public List<PushConfigClientMsgVo> getAppConfigForChanged(@RequestParam String appCode,
                                                      @RequestParam String secretKey,
                                                      @RequestParam String keyParam) {
        ApplicationPo applicationPo = this.checkApplication(appCode, secretKey);
        List<AppConfigInfo> configParams = JsonUtils.toList(keyParam, AppConfigInfo.class);

        List<ApplicationConfigPo> applicationConfigPos = applicationConfigService.lambdaQuery()
                .eq(ApplicationConfigPo::getApplicationId, applicationPo.getId())
                .in(ApplicationConfigPo::getKey, configParams.stream().map(AppConfigInfo::getKey).collect(Collectors.toSet()))
                .list();
        if (CollUtil.isEmpty(applicationConfigPos)) {
            return Collections.emptyList();
        }

        Map<String, ApplicationConfigPo> keyApplicationConfigMap = applicationConfigPos.stream()
                .collect(Collectors.toMap(ApplicationConfigPo::getKey, Function.identity()));

        List<PushConfigClientMsgVo> changeMsgVos = new ArrayList<>();
        for (AppConfigInfo configParam : configParams) {
            ApplicationConfigPo applicationConfigPo = keyApplicationConfigMap.get(configParam.getKey());
            PushConfigClientMsgVo msgVo = new PushConfigClientMsgVo();
            msgVo.setAppCode(applicationPo.getAppCode());
            msgVo.setKey(configParam.getKey());

            if (applicationConfigPo == null) {
                // 删除了
                msgVo.setMsgType(PushConfigMsgType.CONFIG_DELETE.name());
                msgVo.setVersion(0);
                msgVo.setForceUpdate(true);
            } else {
                if (configParam.getVersion() >= applicationConfigPo.getVersion()) {
                    continue;
                }
                // 变化了
                msgVo.setMsgType(PushConfigMsgType.CONFIG_UPDATE.name());
                msgVo.setValue(applicationConfigPo.getValue());
                msgVo.setVersion(applicationConfigPo.getVersion());
                msgVo.setForceUpdate(false);
            }
            changeMsgVos.add(msgVo);
        }

        return changeMsgVos;
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
    public DeferredResult<List<PushConfigClientMsgVo>> watchAppCode(@RequestParam String appCode,
                                                                    @RequestParam String secretKey,
                                                                    @RequestParam Long timeout,
                                                                    @RequestParam List<String> keys,
                                                                    HttpServletResponse response) {
        DeferredResult<List<PushConfigClientMsgVo>> result = new DeferredResult<>(timeout * 1000, Collections.emptyList());
        this.checkApplication(appCode, secretKey);

        String clientId = appCode + "_" + UUID.randomUUID().toString().replaceAll("-", "");
        LongPollingContext.add(clientId, appCode, result, keys);

        result.onTimeout(() -> {
            response.setStatus(HttpStatus.NOT_MODIFIED.value());
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
