package com.hcc.config.center.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HealthController
 *
 * @author hushengjun
 * @date 2022/10/6
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public String health() {
        return "I'm health";
    }

}
