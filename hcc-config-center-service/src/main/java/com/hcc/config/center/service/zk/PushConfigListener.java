package com.hcc.config.center.service.zk;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.hcc.config.center.domain.constants.NodePathConstants;
import com.hcc.config.center.domain.enums.AppModeEnum;
import com.hcc.config.center.domain.vo.PushConfigClientMsgVo;
import com.hcc.config.center.domain.vo.PushConfigNodeDataVo;
import com.hcc.config.center.service.context.LongPollingContext;
import com.hcc.config.center.service.context.NettyChannelContext;
import com.hcc.config.center.service.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * PushConfigListener
 *
 * @author shengjun.hu
 * @date 2022/10/19
 */
@Slf4j
@Component
public class PushConfigListener implements ApplicationListener<ApplicationReadyEvent>, DisposableBean {

    private static final Pattern pathPattern = Pattern.compile("^" + NodePathConstants.PUSH_CONFIG_PATH +"/.+/.+$");

    @Autowired
    private CuratorFramework curatorFramework;

    private TreeCache treeCache;

    @PostConstruct
    private void init() {
        this.treeCache = new TreeCache(curatorFramework, NodePathConstants.PUSH_CONFIG_PATH);
    }

    private void startListener() {
        treeCache.getListenable()
                .addListener((client, event) -> {
                    TreeCacheEvent.Type type = event.getType();
                    if (event.getData() == null) {
                        return;
                    }
                    String path = event.getData().getPath();
                    if (!pathPattern.matcher(path).matches()) {
                        // ???????????????????????????????????????
                        log.info("??????{}????????????????????????{}???????????????????????????", path, type);
                        return;
                    }

                    if (TreeCacheEvent.Type.NODE_ADDED.equals(type)
                            || TreeCacheEvent.Type.NODE_UPDATED.equals(type)) {
                        String jsonData = new String(event.getData().getData(), StandardCharsets.UTF_8);
                        log.info("??????{}????????????????????????{}?????????{}", path, type, jsonData);
                        this.pushToClient(path, jsonData);
                    } else {
                        log.info("??????{}????????????????????????{}?????????", path, type);
                    }
                });
        try {
            treeCache.start();
        } catch (Exception e) {
            throw new IllegalStateException("???????????????????????????", e);
        }
    }

    private void pushToClient(String path, String jsonData) {
        if (StrUtil.isEmpty(jsonData)) {
            log.info("?????????{}?????????????????????", path);
            return;
        }

        PushConfigNodeDataVo nodeDataVo = JsonUtils.toObject(jsonData, PushConfigNodeDataVo.class);

        boolean exist = false;
        if (AppModeEnum.LONG_CONNECT.name().equals(nodeDataVo.getAppMode())) {
            exist = NettyChannelContext.existAppCodeClient(nodeDataVo.getAppCode());
        } else if (AppModeEnum.LONG_POLLING.name().equals(nodeDataVo.getAppMode())) {
            exist = LongPollingContext.existAppCodeClient(nodeDataVo.getAppCode());
        }
        if (!exist) {
            log.info("?????????????????????appCode: [{}]??????????????????????????????", nodeDataVo.getAppCode());
            return;
        }

        if (CollUtil.isNotEmpty(nodeDataVo.getClientIds())) {
            for (String clientId : nodeDataVo.getClientIds()) {
                PushConfigClientMsgVo msgVo = new PushConfigClientMsgVo();
                BeanUtils.copyProperties(nodeDataVo, msgVo);
                msgVo.setClientId(clientId);

                NettyChannelContext.sendMsg(clientId, msgVo);
            }
        } else {
            PushConfigClientMsgVo msgVo = new PushConfigClientMsgVo();
            BeanUtils.copyProperties(nodeDataVo, msgVo);

            if (AppModeEnum.LONG_CONNECT.name().equals(nodeDataVo.getAppMode())) {
                NettyChannelContext.sendMsgToApp(nodeDataVo.getAppCode(), msgVo);
            } else if (AppModeEnum.LONG_POLLING.name().equals(nodeDataVo.getAppMode())) {
                LongPollingContext.publish(nodeDataVo.getAppCode(), msgVo);
            } else {
                throw new IllegalArgumentException(String.format("??????????????????%s", nodeDataVo.getAppMode()));
            }
        }
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        this.startListener();
        log.info("zk path: {}??????????????????", NodePathConstants.PUSH_CONFIG_PATH);
    }

    @Override
    public void destroy() throws Exception {
        if (treeCache != null) {
            treeCache.close();
            log.info("zk path: {}????????????", NodePathConstants.PUSH_CONFIG_PATH);
        }
    }

}
