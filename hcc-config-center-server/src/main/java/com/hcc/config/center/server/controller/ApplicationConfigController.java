package com.hcc.config.center.server.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hcc.config.center.domain.enums.AppModeEnum;
import com.hcc.config.center.domain.enums.AppStatusEnum;
import com.hcc.config.center.domain.param.ApplicationConfigParam;
import com.hcc.config.center.domain.param.ApplicationConfigQueryParam;
import com.hcc.config.center.domain.po.ApplicationConfigPo;
import com.hcc.config.center.domain.po.ApplicationPo;
import com.hcc.config.center.domain.result.PageResult;
import com.hcc.config.center.domain.vo.ApplicationConfigExportVo;
import com.hcc.config.center.domain.vo.ApplicationConfigVo;
import com.hcc.config.center.server.config.IgnoreRestResult;
import com.hcc.config.center.service.ApplicationConfigPushService;
import com.hcc.config.center.service.ApplicationConfigService;
import com.hcc.config.center.service.ApplicationService;
import com.hcc.config.center.service.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ApplicationConfigController
 *
 * @author hushengjun
 * @date 2022/10/6
 */
@Slf4j
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

        boolean valueChanged = applicationConfigService.saveOrUpdateConfig(applicationConfigPo);

        ApplicationPo applicationPo = applicationService.getById(applicationConfigPo.getApplicationId());
        ApplicationConfigPo newAppConfigPo = applicationConfigService.getById(applicationConfigPo.getId());

        // 长轮询模式变更后直接推送
        if (AppModeEnum.LONG_POLLING.name().equals(applicationPo.getAppMode())
                && AppStatusEnum.ONLINE.name().equals(applicationPo.getAppStatus())
                && newAppConfigPo.getDynamic()
                && valueChanged) {
            applicationConfigPushService.pushConfig(applicationConfigPo.getId(), false);
        }
    }

    @PostMapping("/import")
    private String importConfig(@RequestParam Long applicationId, @RequestParam MultipartFile file) {
        ApplicationPo applicationPo = applicationService.getById(applicationId);
        if (applicationPo == null) {
            throw new IllegalArgumentException("应用不存在！");
        }

        String filename = file.getOriginalFilename();
        List<ApplicationConfigPo> applicationConfigPos = new ArrayList<>();
        String content;
        try {
            content = new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("导入异常！", e);
        }
        if (StrUtil.isEmpty(content)) {
            throw new IllegalArgumentException("文件内容为空！");
        }
        if (filename.endsWith("json")) {
            applicationConfigPos.addAll(JsonUtils.toList(content, ApplicationConfigPo.class));
        } else if (filename.endsWith("yml")) {

        } else if (filename.endsWith("properties")) {

        }

        List<String> duplicateKeys = new ArrayList<>();
        for (ApplicationConfigPo c : applicationConfigPos) {
            c.setVersion(c.getVersion() == null ? 1 : c.getVersion());
            c.setDynamic(c.getDynamic() != null && c.getDynamic());
            ApplicationConfigPo existConfigPo = applicationConfigService.lambdaQuery()
                    .select(ApplicationConfigPo::getId)
                    .eq(ApplicationConfigPo::getApplicationId, applicationId)
                    .eq(ApplicationConfigPo::getKey, c.getKey())
                    .one();
            if (existConfigPo != null) {
                duplicateKeys.add(c.getKey());
            }
        }

        if (!CollUtil.isEmpty(duplicateKeys)) {
            throw new IllegalArgumentException("导入失败！以下key已存在：[" + String.join(", ", duplicateKeys) + "]");
        }

        applicationConfigService.saveBatch(applicationConfigPos);

        return null;
    }

    @IgnoreRestResult
    @GetMapping("/export")
    private void exportConfig(@RequestParam Long applicationId, HttpServletResponse response) {
        ApplicationPo applicationPo = applicationService.getById(applicationId);
        if (applicationPo == null) {
            throw new IllegalArgumentException("应用不存在！");
        }

        LambdaQueryWrapper<ApplicationConfigPo> queryWrapper = new LambdaQueryWrapper<ApplicationConfigPo>()
                .eq(ApplicationConfigPo::getApplicationId, applicationId)
                .orderByAsc(ApplicationConfigPo::getDynamic)
                .orderByDesc(ApplicationConfigPo::getUpdateTime)
                .orderByDesc(ApplicationConfigPo::getId);

        List<ApplicationConfigPo> applicationConfigPos = applicationConfigService.list(queryWrapper);
        if (CollUtil.isEmpty(applicationConfigPos)) {
            throw new IllegalArgumentException("没有配置可导出！");
        }
        List<ApplicationConfigExportVo> result = applicationConfigPos.stream()
                .map(c -> {
                    ApplicationConfigExportVo exportVo = new ApplicationConfigExportVo();
                    BeanUtils.copyProperties(c, exportVo);

                    return exportVo;
                })
                .collect(Collectors.toList());

        String json = JsonUtils.toJsonForBeauty(result);
        String fileName = applicationPo.getAppCode() + "_" + "config.json";
        try {
            response.reset();
            response.setContentType("application/octet-stream;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(json.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            throw new IllegalStateException("导出失败！", e);
        }
    }

    @GetMapping("/delete/{id}")
    public void delete(@PathVariable("id") Long id) {
        ApplicationConfigPo applicationConfigPo = this.checkApplicationConfigExist(id);
        ApplicationPo applicationPo = applicationService.getById(applicationConfigPo.getApplicationId());
        if (applicationConfigPo.getDynamic() && AppStatusEnum.ONLINE.name().equals(applicationPo.getAppStatus())) {
            applicationConfigPushService.pushDeletedConfig(id);
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
        if (!AppModeEnum.LONG_CONNECT.name().equals(applicationPo.getAppMode())) {
            throw new IllegalArgumentException("应用模式不是推模式，无法推送");
        }

        applicationConfigPushService.pushConfig(id, forceUpdate);
    }

}
