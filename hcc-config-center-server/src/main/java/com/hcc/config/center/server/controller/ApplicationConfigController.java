package com.hcc.config.center.server.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hcc.config.center.domain.enums.AppModeEnum;
import com.hcc.config.center.domain.enums.AppStatusEnum;
import com.hcc.config.center.domain.param.ApplicationConfigParam;
import com.hcc.config.center.domain.param.ApplicationConfigQueryParam;
import com.hcc.config.center.domain.po.ApplicationConfigPo;
import com.hcc.config.center.domain.po.ApplicationPo;
import com.hcc.config.center.domain.result.PageResult;
import com.hcc.config.center.domain.vo.AppConfigInfo;
import com.hcc.config.center.domain.vo.ApplicationConfigVo;
import com.hcc.config.center.server.context.LongPollingContext;
import com.hcc.config.center.service.ApplicationConfigPushService;
import com.hcc.config.center.service.ApplicationConfigService;
import com.hcc.config.center.service.ApplicationService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    private ApplicationConfigPushService applicationConfigPushService;

    @PostMapping("/page")
    public PageResult<ApplicationConfigVo> page(@RequestBody ApplicationConfigQueryParam param) {
        LambdaQueryWrapper<ApplicationConfigPo> queryWrapper = new LambdaQueryWrapper<ApplicationConfigPo>()
                .eq(ApplicationConfigPo::getApplicationId, param.getApplicationId())
                .like(StrUtil.isNotEmpty(param.getKey()), ApplicationConfigPo::getKey, param.getKey())
                .eq(ApplicationConfigPo::getDynamic, param.getDynamic())
                .orderByDesc(ApplicationConfigPo::getUpdateTime)
                .orderByDesc(ApplicationConfigPo::getId);

        PageResult<ApplicationConfigPo> pageResult = applicationConfigService.page(param, queryWrapper);

        return pageResult.convertToTarget(ApplicationConfigVo.class);
    }

    private ApplicationConfigPo checkApplicationConfigExist(Long id) {
        ApplicationConfigPo existApplicationConfigPo = applicationConfigService.getById(id);
        if (existApplicationConfigPo == null) {
            throw new IllegalArgumentException("配置不存在");
        }

        return existApplicationConfigPo;
    }

    @PostMapping("/save")
    public void save(@RequestBody ApplicationConfigParam param) {
        ApplicationConfigPo applicationConfigPo = new ApplicationConfigPo();
        BeanUtils.copyProperties(param, applicationConfigPo);

        applicationConfigService.saveOrUpdateConfig(applicationConfigPo);

        ApplicationPo applicationPo = applicationService.getById(applicationConfigPo.getApplicationId());
        ApplicationConfigPo newAppConfigPo = applicationConfigService.getById(applicationConfigPo.getId());
        if (AppModeEnum.PULL.name().equals(applicationPo.getAppMode())
                && AppStatusEnum.ONLINE.name().equals(applicationPo.getAppStatus())
                && newAppConfigPo.getDynamic()) {
            AppConfigInfo appConfigInfo = new AppConfigInfo();
            BeanUtils.copyProperties(newAppConfigPo, appConfigInfo);
            appConfigInfo.setAppCode(applicationPo.getAppCode());

            LongPollingContext.publish(appConfigInfo);
        }
    }

    @PostMapping("/import")
    private void importConfig(@RequestParam Long applicationId, @RequestParam MultipartFile file) {}

    @PostMapping("/export")
    private void exportConfig(@RequestParam Long applicationId) {}

    @GetMapping("/delete/{id}")
    public void delete(@PathVariable("id") Long id) {
        ApplicationConfigPo applicationConfigPo = this.checkApplicationConfigExist(id);
        ApplicationPo applicationPo = applicationService.getById(applicationConfigPo.getApplicationId());
        if (applicationConfigPo.getDynamic() && AppStatusEnum.ONLINE.name().equals(applicationPo.getAppStatus())) {
            if (AppModeEnum.PUSH.name().equals(applicationPo.getAppMode())) {
                applicationConfigPushService.pushDeletedConfig(id);
            } else {
                applicationConfigService.removeById(id);

                AppConfigInfo appConfigInfo = new AppConfigInfo();
                appConfigInfo.setAppCode(applicationPo.getAppCode());
                appConfigInfo.setDynamic(true);
                appConfigInfo.setKey(applicationConfigPo.getKey());
                appConfigInfo.setVersion(0);

                LongPollingContext.publish(appConfigInfo);
            }
        } else {
            applicationConfigService.removeById(id);
        }
    }

    @GetMapping("/push/{id}")
    public void push(@PathVariable("id") Long id, @RequestParam(required = false) Boolean forceUpdate) {
        ApplicationConfigPo applicationConfigPo = this.checkApplicationConfigExist(id);
        if (!applicationConfigPo.getDynamic()) {
            throw new IllegalArgumentException("非动态配置，无法推送");
        }
        ApplicationPo applicationPo = applicationService.getById(applicationConfigPo.getApplicationId());
        if (!AppStatusEnum.ONLINE.name().equals(applicationPo.getAppStatus())) {
            throw new IllegalArgumentException("应用未上线，无法推送");
        }
        if (!AppModeEnum.PUSH.name().equals(applicationPo.getAppMode())) {
            throw new IllegalArgumentException("应用模式不是推模式，无法推送");
        }

        applicationConfigPushService.pushConfig(id, forceUpdate);
    }

}
