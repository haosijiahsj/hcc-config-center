package com.hcc.config.center.service.zk;

import com.hcc.config.center.domain.constants.NodePathConstants;
import com.hcc.config.center.domain.vo.PushConfigNodeDataVo;
import com.hcc.config.center.service.netty.NettyChannelManage;
import com.hcc.config.center.service.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * PushConfigListener
 *
 * @author shengjun.hu
 * @date 2022/10/19
 */
@Slf4j
public class PushConfigListener {

    @Autowired
    private CuratorFramework curatorFramework;

    private void startListener() {
        PathChildrenCache pathChildrenCache = new PathChildrenCache(curatorFramework, NodePathConstants.PUSH_CONFIG_PATH, true);
        pathChildrenCache.getListenable()
                .addListener((client, event) -> {
                    PathChildrenCacheEvent.Type eventType = event.getType();
                    byte[] data = event.getData().getData();
                    String jsonData = new String(data, StandardCharsets.UTF_8);
                    log.info("节点{}变化，值：{}", event.getData().getPath(), jsonData);

                    PushConfigNodeDataVo nodeDataVo = JsonUtils.toObject(jsonData, PushConfigNodeDataVo.class);

                    NettyChannelManage.sendMsgToApp(nodeDataVo.getAppCode(), null);
                });
        try {
            pathChildrenCache.start();
        } catch (Exception e) {
            throw new IllegalStateException("启动节点监听失败！", e);
        }
    }

}
