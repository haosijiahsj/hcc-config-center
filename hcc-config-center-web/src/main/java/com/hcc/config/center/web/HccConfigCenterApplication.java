package com.hcc.config.center.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * HccConfigCenterApplication
 *
 * @author hushengjun
 * @date 2022/10/6
 */
@Slf4j
@SpringBootApplication(scanBasePackages = "com.hcc.config.center")
public class HccConfigCenterApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(HccConfigCenterApplication.class)
                .bannerMode(Banner.Mode.OFF)
                .run(args);

        log.info("hcc-config-center-web启动成功！");
    }

}
