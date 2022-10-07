package com.hcc.config.center.web.controller;

import com.hcc.config.center.service.ApplicationConfigHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
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


}
