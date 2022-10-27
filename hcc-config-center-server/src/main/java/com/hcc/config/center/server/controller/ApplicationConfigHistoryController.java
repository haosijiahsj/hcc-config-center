package com.hcc.config.center.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hcc.config.center.domain.param.ApplicationConfigHistoryQueryParam;
import com.hcc.config.center.domain.po.ApplicationConfigHistoryPo;
import com.hcc.config.center.domain.result.PageResult;
import com.hcc.config.center.domain.vo.ApplicationConfigHistoryVo;
import com.hcc.config.center.service.ApplicationConfigHistoryService;
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
@RequestMapping("/application-config-history")
public class ApplicationConfigHistoryController {

    @Autowired
    private ApplicationConfigHistoryService applicationConfigHistoryService;

    @PostMapping("/page")
    public PageResult<ApplicationConfigHistoryVo> page(@RequestBody ApplicationConfigHistoryQueryParam param) {
        LambdaQueryWrapper<ApplicationConfigHistoryPo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApplicationConfigHistoryPo::getApplicationConfigId, param.getApplicationConfigId());
        queryWrapper.orderByDesc(ApplicationConfigHistoryPo::getId);

        return applicationConfigHistoryService.page(param, queryWrapper)
                .convertToTarget(ApplicationConfigHistoryVo.class);
    }

}
