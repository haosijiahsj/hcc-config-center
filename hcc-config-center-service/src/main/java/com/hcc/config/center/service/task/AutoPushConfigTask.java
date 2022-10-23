package com.hcc.config.center.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * AutoPushConfigTask
 *
 * @author shengjun.hu
 * @date 2022/10/20
 */
@Slf4j
@Component
public class AutoPushConfigTask {

    @Scheduled(cron = "*/15 * * * * ?")
    public void execute() {
    }

}
