package com.hcc.config.center.server.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hcc.config.center.domain.param.ApplicationConfigParam;
import com.hcc.config.center.domain.param.ApplicationConfigQueryParam;
import com.hcc.config.center.domain.po.ApplicationConfigPo;
import com.hcc.config.center.domain.result.PageResult;
import com.hcc.config.center.domain.vo.ApplicationConfigVo;
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

import java.time.LocalDateTime;

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
            throw new IllegalArgumentException("配置不存在");
        }

        return existApplicationConfigPo;
    }

    @PostMapping("/save")
    public void save(@RequestBody ApplicationConfigParam param) {
        ApplicationConfigPo applicationConfigPo = new ApplicationConfigPo();
        BeanUtils.copyProperties(param, applicationConfigPo);
        LocalDateTime now = LocalDateTime.now();
        applicationConfigPo.setUpdateTime(now);
        if (param.getId() == null) {
            applicationConfigPo.setCreateTime(now);
            applicationConfigPo.setVersion(1);
            applicationConfigService.save(applicationConfigPo);
        } else {
            this.checkApplicationConfigExist(param.getId());
            applicationConfigService.lambdaUpdate()
                    .set(ApplicationConfigPo::getValue, param.getValue())
                    .set(ApplicationConfigPo::getComment, param.getComment())
                    .setSql("version = version + 1")
                    .eq(ApplicationConfigPo::getId, param.getId())
                    .update();
        }
    }

    @PostMapping("/import")
    private void importConfig(@RequestParam String appCode, @RequestParam MultipartFile file) {}

    @PostMapping("/export")
    private void exportConfig(@RequestParam String appCode) {}

    @GetMapping("/delete/{id}")
    public void delete(@PathVariable("id") Long id) {
        ApplicationConfigPo applicationConfigPo = this.checkApplicationConfigExist(id);
        applicationConfigService.removeById(id);

        if (applicationConfigPo.getDynamic()) {
            // TODO 推送
        }
    }

    @GetMapping("/push/{id}")
    public void push(Long id) {
        ApplicationConfigPo applicationConfigPo = this.checkApplicationConfigExist(id);
        if (!applicationConfigPo.getDynamic()) {
            throw new IllegalArgumentException("非动态配置，无法推送");
        }

        // TODO 推送
    }

}
