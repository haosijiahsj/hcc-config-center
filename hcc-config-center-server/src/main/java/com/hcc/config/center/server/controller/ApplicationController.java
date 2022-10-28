package com.hcc.config.center.server.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hcc.config.center.domain.enums.AppModeEnum;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @GetMapping("/all")
    public List<ApplicationVo> all() {
        List<ApplicationPo> applicationPos = applicationService.lambdaQuery()
                .in(ApplicationPo::getAppStatus, Arrays.asList(AppStatusEnum.ONLINE.name(), AppStatusEnum.OFFLINE.name()))
                .orderByDesc(ApplicationPo::getUpdateTime)
                .orderByDesc(ApplicationPo::getId)
                .list();
        if (CollectionUtil.isEmpty(applicationPos)) {
            return Collections.emptyList();
        }

        return applicationPos.stream()
                .map(a -> {
                    ApplicationVo applicationVo = new ApplicationVo();
                    BeanUtils.copyProperties(a, applicationVo);

                    return applicationVo;
                })
                .collect(Collectors.toList());
    }

    @PostMapping("/page")
    public PageResult<ApplicationVo> page(@RequestBody ApplicationQueryParam param) {
        LambdaQueryWrapper<ApplicationPo> queryWrapper = new LambdaQueryWrapper<ApplicationPo>()
                .like(StrUtil.isNotEmpty(param.getAppCode()), ApplicationPo::getAppCode, param.getAppCode())
                .like(StrUtil.isNotEmpty(param.getAppName()), ApplicationPo::getAppName, param.getAppName())
                .orderByDesc(ApplicationPo::getUpdateTime)
                .orderByDesc(ApplicationPo::getId);
        PageResult<ApplicationPo> pageResult = applicationService.page(param, queryWrapper);

        return pageResult.convertToTarget(ApplicationVo.class);
    }

    @PostMapping("/save")
    public void save(@RequestBody ApplicationParam param) {
        ApplicationPo applicationPo = new ApplicationPo();
        BeanUtils.copyProperties(param, applicationPo);

        LocalDateTime now = LocalDateTime.now();
        applicationPo.setUpdateTime(now);
        if (param.getId() == null) {
            ApplicationPo existApplicationPo = applicationService.lambdaQuery()
                    .eq(ApplicationPo::getAppCode, param.getAppCode())
                    .one();
            if (existApplicationPo != null) {
                throw new IllegalArgumentException(String.format("应用编码：[%s]已存在", param.getAppCode()));
            }

            String secretKey = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
            applicationPo.setSecretKey(secretKey);
            applicationPo.setAppStatus(AppStatusEnum.NOT_ONLINE.name());
            if (param.getAppMode() == null) {
                applicationPo.setAppMode(AppModeEnum.LONG_CONNECT.name());
            }
            applicationPo.setCreateTime(now);

            applicationService.save(applicationPo);
        } else {
            ApplicationPo exist = this.checkApplicationExist(param.getId());
            if (!AppStatusEnum.NOT_ONLINE.name().equals(exist.getAppStatus())) {
                if (!exist.getAppCode().equals(param.getAppCode()) || exist.getAppMode().equals(param.getAppMode())) {
                    throw new IllegalArgumentException("当前状态不允许修改应用编码和模式");
                }
            }

            ApplicationPo updatePo = new ApplicationPo();
            BeanUtils.copyProperties(param, updatePo);
            updatePo.setSecretKey(exist.getSecretKey());
            updatePo.setAppStatus(exist.getAppStatus());
            updatePo.setUpdateTime(now);
            applicationService.updateById(updatePo);
        }
    }

    private ApplicationPo checkApplicationExist(Long id) {
        ApplicationPo applicationPo = applicationService.getById(id);
        if (applicationPo == null) {
            throw new IllegalArgumentException("应用不存在");
        }

        return applicationPo;
    }

    @GetMapping("/delete/{id}")
    public void delete(@PathVariable("id") Long id) {
        ApplicationPo applicationPo = this.checkApplicationExist(id);
        if (AppStatusEnum.ONLINE.name().equals(applicationPo.getAppStatus())) {
            throw new IllegalArgumentException("应用已上线，无法删除");
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
        ApplicationPo applicationPo = this.checkApplicationExist(id);
        if (AppStatusEnum.OFFLINE.name().equals(applicationPo.getAppStatus())) {
            throw new IllegalArgumentException("应用已下线");
        }
        if (AppStatusEnum.NOT_ONLINE.name().equals(applicationPo.getAppStatus())) {
            throw new IllegalArgumentException("应用未上线");
        }

        applicationService.lambdaUpdate()
                .set(ApplicationPo::getAppStatus, AppStatusEnum.OFFLINE.name())
                .eq(ApplicationPo::getId, id)
                .update();
    }

}
