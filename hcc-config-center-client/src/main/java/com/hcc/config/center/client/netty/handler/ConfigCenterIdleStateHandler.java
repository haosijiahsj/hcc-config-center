package com.hcc.config.center.client.netty.handler;

import com.hcc.config.center.client.connect.DefaultServerNodeChooser;
import com.hcc.config.center.client.connect.ServerNodeChooser;
import com.hcc.config.center.client.context.ConfigContext;
import com.hcc.config.center.client.entity.ServerNodeInfo;
import com.hcc.config.center.client.netty.ConfigCenterClient;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * ConfigCenterIdleStateHandler
 *
 * @author hushengjun
 * @date 2022/10/22
 */
@Slf4j
public class ConfigCenterIdleStateHandler extends IdleStateHandler {

    private ConfigContext configContext;
    private ServerNodeChooser serverNodeChooser;
    private ConfigCenterClient configCenterClient;

    public ConfigCenterIdleStateHandler(ConfigContext configContext, ConfigCenterClient configCenterClient) {
        this(10, 10, 30, TimeUnit.SECONDS);
        this.configContext = configContext;
        this.serverNodeChooser = new DefaultServerNodeChooser();
        this.configCenterClient = configCenterClient;
    }

    public ConfigCenterIdleStateHandler(long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit) {
        super(readerIdleTime, writerIdleTime, allIdleTime, unit);
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
        super.channelInactive(ctx);
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
                    log.error("重连异常，等待{}s后重试！异常信息：{}", timeout, e.getMessage());
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
