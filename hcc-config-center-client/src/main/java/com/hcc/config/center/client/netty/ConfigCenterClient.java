package com.hcc.config.center.client.netty;

import com.hcc.config.center.client.ConfigRefreshCallBack;
import com.hcc.config.center.client.context.ConfigContext;
import com.hcc.config.center.client.entity.ReceivedServerMsg;
import com.hcc.config.center.client.processor.ConfigCenterMsgProcessor;
import com.hcc.config.center.client.rebalance.ServerNodeChooser;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 客户端，接受服务端消息，动态刷新值
 *
 * @author shengjun.hu
 * @date 2022/10/9
 */
@Slf4j
@Data
public class ConfigCenterClient {

    private String host;
    private int port;
    private ConfigContext configContext;
    private ServerNodeChooser serverNodeChooser;

    private ConfigCenterMsgProcessor configCenterMsgProcessor;
    private NioEventLoopGroup eventLoopGroup;
    private boolean stopFlag = false;

    public ConfigCenterClient(String host, int port, ConfigContext configContext, ConfigRefreshCallBack callBack,
                              ServerNodeChooser serverNodeChooser) {
        this.host = host;
        this.port = port;
        this.configContext = configContext;
        this.serverNodeChooser = serverNodeChooser;
        this.configCenterMsgProcessor = new ConfigCenterMsgProcessor(configContext, callBack);
    }

    /**
     * 启动客户端
     */
    public void startUp() {
        this.doConnect(host, port, false);
    }

    /**
     * 重连服务器
     * @param host
     * @param port
     */
    public void reconnect(String host, int port) {
        this.doConnect(host, port, true);
    }

    /**
     * 关闭客户端
     */
    public void stop() {
        this.stopFlag = true;
        if (eventLoopGroup != null) {
            eventLoopGroup.shutdownGracefully();
        }
        log.info("动态推送客户端关闭！");
    }

    /**
     * 关闭客户端，重平衡
     */
    public void stopForBalance() {
        if (eventLoopGroup != null) {
            eventLoopGroup.shutdownGracefully();
            log.info("动态推送客户端关闭！进行重平衡");
        }
    }

    /**
     * 连接服务器
     * @param host
     * @param port
     * @param isReconnect
     */
    private void doConnect(String host, int port, boolean isReconnect) {
        Bootstrap bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup();
        try {
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ConfigCenterClientChannelInitializer(configContext, configCenterMsgProcessor, this));
            // 启动客户端连接到客户端
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            channelFuture.addListener(this.channelFutureListener(isReconnect));
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            throw new IllegalStateException("动态推送客户端启动失败！", e);
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }

    private ChannelFutureListener channelFutureListener(boolean isReconnect) {
        return future -> {
            if (future.isSuccess()) {
                log.info("动态推送客户端{}成功！", isReconnect ? "重启" : "启动");
                if (isReconnect) {
                    this.refreshConfig();
                    log.info("重连后刷新动态配置成功！");
                }
            }
        };
    }

    /**
     * 刷新配置
     */
    private void refreshConfig() {
        List<ReceivedServerMsg> changedReceivedServerMsgs = configContext.getChangedConfigFromConfigCenter();
        if (StringUtils.isEmpty(changedReceivedServerMsgs)) {
            return;
        }

        changedReceivedServerMsgs.forEach(configCenterMsgProcessor::addMsgToQueue);
    }

}
