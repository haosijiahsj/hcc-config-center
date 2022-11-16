package com.hcc.config.center.client;

import com.hcc.config.center.client.context.ConfigContext;
import com.hcc.config.center.client.entity.AppConfigInfo;
import com.hcc.config.center.client.utils.CollUtils;

import java.util.Collections;
import java.util.List;

/**
 * 直接从服务端获取配置
 *
 * @author hushengjun
 * @date 2022/11/13
 */
public class RemoteConfigServiceImpl implements ConfigService {

    private final ConfigContext configContext;

    public RemoteConfigServiceImpl(ConfigContext configContext) {
        this.configContext = configContext;
    }

    @Override
    public AppConfigInfo getConfigInfo(String key) {
        List<AppConfigInfo> appConfigInfos = configContext.getConfigFromConfigCenter(Collections.singletonList(key));
        if (CollUtils.isEmpty(appConfigInfos)) {
            return null;
        }

        return appConfigInfos.get(0);
    }

}
