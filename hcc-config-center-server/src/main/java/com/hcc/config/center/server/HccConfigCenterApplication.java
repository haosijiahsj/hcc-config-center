package com.hcc.config.center.server;

import com.hcc.config.center.service.config.ConfigCenterProperties;
import com.hcc.config.center.server.netty.ConfigCenterServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * HccConfigCenterApplication
 *
 * @author hushengjun
 * @date 2022/10/6
 */
@Slf4j
@EnableScheduling
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
        String host;
        try {
            host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new IllegalStateException("获取host异常", e);
        }
        // 启动动态配置推送服务
        ConfigCenterServer configCenterServer = new ConfigCenterServer(host, configCenterProperties.getServerPort());

        new Thread(configCenterServer::startUp).start();
    }

}
