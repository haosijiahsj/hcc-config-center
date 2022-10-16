package com.hcc.config.center.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hcc.config.center.domain.enums.AppStatusEnum;
import com.hcc.config.center.domain.param.ApplicationParam;
import com.hcc.config.center.domain.param.ApplicationQueryParam;
import com.hcc.config.center.domain.po.ApplicationConfigPo;
import com.hcc.config.center.domain.po.ApplicationPo;
import com.hcc.config.center.domain.result.PageResult;
import com.hcc.config.center.domain.vo.ApplicationVo;
import com.hcc.config.center.service.ApplicationConfigService;
import com.hcc.config.center.service.ApplicationService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ApplicationController
 *
 * @author hushengjun
 * @date 2022/10/6
 */
@RestController
@RequestMapping("/application")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationConfigService applicationConfigService;

    @GetMapping("/get/{id}")
    public ApplicationVo get(@PathVariable("id") Long id) {
        ApplicationPo applicationPo = applicationService.getById(id);
        if (applicationPo == null) {
            return null;
        }
        ApplicationVo applicationVo = new ApplicationVo();
        BeanUtils.copyProperties(applicationPo, applicationVo);

        return applicationVo;
    }

    @PostMapping("/page")
    public PageResult<ApplicationVo> page(@RequestBody ApplicationQueryParam param) {
        LambdaQueryWrapper<ApplicationPo> queryWrapper = new LambdaQueryWrapper<ApplicationPo>()
                .like(param.getAppName() != null, ApplicationPo::getAppName, param.getAppName())
                .orderByDesc(ApplicationPo::getUpdateTime)
                .orderByDesc(ApplicationPo::getId)
                .orderByAsc(ApplicationPo::getAppCode);
        PageResult<ApplicationPo> pageResult = applicationService.page(param, queryWrapper);

        return pageResult.convertToTarget(ApplicationVo.class);
    }

    @PostMapping("/save")
    public void save(@RequestBody ApplicationParam param) {
        ApplicationPo applicationPo = new ApplicationPo();
        BeanUtils.copyProperties(param, applicationPo);

        LocalDateTime now = LocalDateTime.now();
        if (param.getId() == null) {
            String secretKey = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
            applicationPo.setSecretKey(secretKey);
            applicationPo.setAppStatus(AppStatusEnum.NOT_ONLINE.name());
            applicationPo.setCreateTime(now);
            applicationPo.setUpdateTime(now);
            applicationPo.setDeleted(0);
            applicationService.save(applicationPo);
        } else {
            applicationPo.setUpdateTime(now);
            applicationService.updateById(applicationPo);
        }
    }

    @GetMapping("/delete/{id}")
    public void delete(@PathVariable("id") Long id) {
        ApplicationPo applicationPo = applicationService.getById(id);
        if (applicationPo == null) {
            throw new IllegalArgumentException("应用不存在");
        }
        ApplicationConfigPo applicationConfigPo = applicationConfigService.lambdaQuery()
                .eq(ApplicationConfigPo::getApplicationId, id)
                .last("LIMIT 1")
                .one();
        if (applicationConfigPo != null) {
            throw new IllegalArgumentException("应用存在配置，无法删除");
        }
        applicationService.removeById(id);
    }

    @GetMapping("/online/{id}")
    public void online(@PathVariable("id") Long id) {
        ApplicationPo applicationPo = applicationService.getById(id);
        if (applicationPo == null) {
            throw new IllegalArgumentException("应用不存在");
        }
        if (AppStatusEnum.ONLINE.name().equals(applicationPo.getAppStatus())) {
            throw new IllegalArgumentException("应用已上线");
        }

        applicationService.lambdaUpdate()
                .set(ApplicationPo::getAppStatus, AppStatusEnum.ONLINE.name())
                .eq(ApplicationPo::getId, id)
                .update();
    }

    @GetMapping("/offline/{id}")
    public void offline(@PathVariable("id") Long id) {
        ApplicationPo applicationPo = applicationService.getById(id);
        if (applicationPo == null) {
            throw new IllegalArgumentException("应用不存在");
        }
        if (AppStatusEnum.OFFLINE.name().equals(applicationPo.getAppStatus())) {
            throw new IllegalArgumentException("应用已下线");
        }

        applicationService.lambdaUpdate()
                .set(ApplicationPo::getAppStatus, AppStatusEnum.OFFLINE.name())
                .eq(ApplicationPo::getId, id)
                .update();
    }

}
