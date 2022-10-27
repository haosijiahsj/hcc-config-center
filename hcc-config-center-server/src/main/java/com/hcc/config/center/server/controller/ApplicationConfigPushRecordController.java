package com.hcc.config.center.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hcc.config.center.domain.param.ApplicationConfigPushRecordQueryParam;
import com.hcc.config.center.domain.po.ApplicationConfigPushRecordPo;
import com.hcc.config.center.domain.result.PageResult;
import com.hcc.config.center.domain.vo.ApplicationConfigPushRecordVo;
import com.hcc.config.center.service.ApplicationConfigPushRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ApplicationConfigHistoryController
 *
 * @author hushengjun
 * @date 2022/10/6
 */
@RestController
@RequestMapping("/application-config-push-record")
public class ApplicationConfigPushRecordController {

    @Autowired
    private ApplicationConfigPushRecordService applicationConfigPushRecordService;

    @PostMapping("/page")
    public PageResult<ApplicationConfigPushRecordVo> page(@RequestBody ApplicationConfigPushRecordQueryParam param) {
        LambdaQueryWrapper<ApplicationConfigPushRecordPo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApplicationConfigPushRecordPo::getApplicationConfigId, param.getApplicationConfigId());
        queryWrapper.orderByDesc(ApplicationConfigPushRecordPo::getId);

        return applicationConfigPushRecordService.page(param, queryWrapper)
                .convertToTarget(ApplicationConfigPushRecordVo.class);
    }

}
