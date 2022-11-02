package com.hcc.config.center.client.netty;

import com.hcc.config.center.client.balance.DefaultServerNodeChooser;
import com.hcc.config.center.client.balance.ServerNodeChooser;
import com.hcc.config.center.client.context.ConfigContext;
import com.hcc.config.center.client.entity.ServerNodeInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 在此实现了重连
 *
 * @author hushengjun
 * @date 2022/10/22
 */
@Slf4j
public class ConfigCenterIdleStateHandler extends IdleStateHandler {

    private final ConfigContext configContext;
    private final ServerNodeChooser serverNodeChooser;
    private final ConfigCenterClient configCenterClient;

    public ConfigCenterIdleStateHandler(ConfigContext configContext, ConfigCenterClient configCenterClient) {
        super(10, 10, 30, TimeUnit.SECONDS);
        this.configContext = configContext;
        this.serverNodeChooser = new DefaultServerNodeChooser();
        this.configCenterClient = configCenterClient;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("连接推送服务器：{}成功", ctx.channel().remoteAddress());
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (configCenterClient.isStopFlag()) {
            // 遇停止标识则表示服务在关闭，不进行重连
            return;
        }
        log.error("服务端离线：{}，尝试重连！", ctx.channel().remoteAddress());

        Runnable runnable = () -> {
            int count = 0;
            long curTimeout = 5L;
            while (true) {
                try {
                    this.reconnect();
                    break;
                } catch (Exception e) {
                    long timeout = curTimeout + count * 2L;
                    log.error(String.format("重连异常，等待%ss后重试！", timeout), e);
                    count++;
                    curTimeout = timeout;
                    sleepForSecond(timeout);
                }
            }
        };
        new Thread(runnable).start();
    }

    /**
     * 重连服务端
     * @return
     */
    private void reconnect() {
        configContext.refreshServerNode();
        ServerNodeInfo serverNodeInfo = serverNodeChooser.chooseServerNode(configContext);
        if (serverNodeInfo == null) {
            throw new IllegalStateException("没有获取到配置中心服务节点");
        }
        configCenterClient.reconnect(serverNodeInfo.getHost(), serverNodeInfo.getPort());
    }

    /**
     * 休眠
     * @param timeout
     */
    private void sleepForSecond(long timeout) {
        try {
            TimeUnit.SECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            log.error("sleep异常！", e);
        }
    }

}
