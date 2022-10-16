package com.hcc.config.center.server;

import com.hcc.config.center.server.config.ConfigCenterProperties;
import com.hcc.config.center.server.netty.ConfigCenterServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

/**
 * HccConfigCenterApplication
 *
 * @author hushengjun
 * @date 2022/10/6
 */
@Slf4j
@SpringBootApplication(scanBasePackages = "com.hcc.config.center")
public class HccConfigCenterApplication implements ApplicationListener<ApplicationStartedEvent> {

    @Autowired
    private ConfigCenterProperties configCenterProperties;

    public static void main(String[] args) {
        new SpringApplicationBuilder(HccConfigCenterApplication.class)
                .bannerMode(Banner.Mode.OFF)
                .run(args);

        log.info("hcc-config-center-server启动成功！");
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {
        // 启动动态配置推送服务
        ConfigCenterServer configCenterServer = new ConfigCenterServer(configCenterProperties.getPort());

        new Thread(configCenterServer::startUp).start();
    }

}
