package com.hcc.config.center.server.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.yaml.YamlUtil;
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
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
                .orderByDesc(ApplicationConfigPo::getUpdateTime)
                .orderByDesc(ApplicationConfigPo::getId);

        PageResult<ApplicationConfigPo> pageResult = applicationConfigService.page(param, queryWrapper);

        return pageResult.convertToTarget(ApplicationConfigVo.class);
    }

    private ApplicationConfigPo checkApplicationConfigExist(Long id) {
        ApplicationConfigPo existApplicationConfigPo = applicationConfigService.getById(id);
        if (existApplicationConfigPo == null) {
            throw new IllegalArgumentException("???????????????");
        }

        return existApplicationConfigPo;
    }

    @PostMapping("/save")
    public void save(@RequestBody ApplicationConfigParam param) {
        param.check();
        ApplicationConfigPo applicationConfigPo = new ApplicationConfigPo();
        BeanUtils.copyProperties(param, applicationConfigPo);

        boolean valueChanged = applicationConfigService.saveOrUpdateConfig(applicationConfigPo);

        ApplicationPo applicationPo = applicationService.getById(applicationConfigPo.getApplicationId());
        ApplicationConfigPo newAppConfigPo = applicationConfigService.getById(applicationConfigPo.getId());

        // ????????????????????????????????????
        if (AppModeEnum.LONG_POLLING.name().equals(applicationPo.getAppMode())
                && AppStatusEnum.ONLINE.name().equals(applicationPo.getAppStatus())
                && valueChanged) {
            applicationConfigPushService.pushConfig(applicationConfigPo.getId(), false);
        }
    }

    @PostMapping("/import")
    private void importConfig(@RequestParam Long applicationId, @RequestParam MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("??????????????????");
        }
        if (!filename.endsWith("json") && !filename.endsWith("yml") && !filename.endsWith("properties")) {
            throw new IllegalArgumentException("?????????json???yml???properties??????????????????");
        }

        ApplicationPo applicationPo = applicationService.getById(applicationId);
        if (applicationPo == null) {
            throw new IllegalArgumentException("??????????????????");
        }

        List<ApplicationConfigPo> applicationConfigPos = new ArrayList<>();
        try {
            if (filename.endsWith("json")) {
                String json = new String(file.getBytes(), StandardCharsets.UTF_8);
                applicationConfigPos.addAll(JsonUtils.toList(json, ApplicationConfigPo.class));
            } else if (filename.endsWith("yml")) {
                Map<String, String> map = this.parseYml(file.getInputStream());
                map.forEach((k, v) -> {
                    ApplicationConfigPo configPo = new ApplicationConfigPo();
                    configPo.setKey(k);
                    configPo.setValue(v);
                    applicationConfigPos.add(configPo);
                });
            } else if (filename.endsWith("properties")) {
                Properties properties = new Properties();
                properties.load(file.getInputStream());
                properties.forEach((k, v) -> {
                    ApplicationConfigPo configPo = new ApplicationConfigPo();
                    configPo.setKey(k.toString());
                    configPo.setValue(v.toString());
                    applicationConfigPos.add(configPo);
                });
            }
        } catch (Exception e) {
            throw new IllegalStateException("???????????????", e);
        }

        LocalDateTime now = LocalDateTime.now();
        List<String> duplicateKeys = new ArrayList<>();
        for (ApplicationConfigPo configPo : applicationConfigPos) {
            configPo.setApplicationId(applicationId);
            configPo.setVersion(configPo.getVersion() == null ? 1 : configPo.getVersion());
            configPo.setCreateTime(now);
            configPo.setUpdateTime(now);
            ApplicationConfigPo existConfigPo = applicationConfigService.lambdaQuery()
                    .select(ApplicationConfigPo::getId)
                    .eq(ApplicationConfigPo::getApplicationId, applicationId)
                    .eq(ApplicationConfigPo::getKey, configPo.getKey())
                    .one();
            if (existConfigPo != null) {
                duplicateKeys.add(configPo.getKey());
            }
        }

        if (!CollUtil.isEmpty(duplicateKeys)) {
            throw new IllegalArgumentException(String.format("key: [%s]?????????", String.join(", ", duplicateKeys)));
        }

        applicationConfigPos.forEach(applicationConfigService::saveOrUpdateConfig);
    }

    private Map<String, String> parseYml(InputStream inputStream) {
        Map map = YamlUtil.load(inputStream, Map.class);

        Map<String, String> resultMap = new HashMap<>();

        return resultMap;
    }

    @IgnoreRestResult
    @GetMapping("/export")
    private void exportConfig(@RequestParam Long applicationId, HttpServletResponse response) {
        ApplicationPo applicationPo = applicationService.getById(applicationId);
        if (applicationPo == null) {
            throw new IllegalArgumentException("??????????????????");
        }

        LambdaQueryWrapper<ApplicationConfigPo> queryWrapper = new LambdaQueryWrapper<ApplicationConfigPo>()
                .eq(ApplicationConfigPo::getApplicationId, applicationId)
                .orderByDesc(ApplicationConfigPo::getUpdateTime)
                .orderByDesc(ApplicationConfigPo::getId);

        List<ApplicationConfigPo> applicationConfigPos = applicationConfigService.list(queryWrapper);
        if (CollUtil.isEmpty(applicationConfigPos)) {
            throw new IllegalArgumentException("????????????????????????");
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
            throw new IllegalStateException("???????????????", e);
        }
    }

    @GetMapping("/delete/{id}")
    public void delete(@PathVariable("id") Long id) {
        ApplicationConfigPo applicationConfigPo = this.checkApplicationConfigExist(id);
        ApplicationPo applicationPo = applicationService.getById(applicationConfigPo.getApplicationId());
        if (AppStatusEnum.ONLINE.name().equals(applicationPo.getAppStatus())) {
            applicationConfigPushService.pushDeletedConfig(id);
        } else {
            applicationConfigService.removeById(id);
        }
    }

    @GetMapping("/push/{id}")
    public void push(@PathVariable("id") Long id, @RequestParam(required = false) Boolean forceUpdate) {
        ApplicationConfigPo applicationConfigPo = this.checkApplicationConfigExist(id);
        ApplicationPo applicationPo = applicationService.getById(applicationConfigPo.getApplicationId());
        if (!AppStatusEnum.ONLINE.name().equals(applicationPo.getAppStatus())) {
            throw new IllegalArgumentException("??????????????????????????????");
        }
        if (!AppModeEnum.LONG_CONNECT.name().equals(applicationPo.getAppMode())) {
            throw new IllegalArgumentException("??????????????????????????????????????????");
        }

        applicationConfigPushService.pushConfig(id, forceUpdate);
    }

}
