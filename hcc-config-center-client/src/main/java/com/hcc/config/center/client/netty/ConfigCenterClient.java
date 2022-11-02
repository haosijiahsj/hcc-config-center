package com.hcc.config.center.client.netty;

import com.hcc.config.center.client.ProcessFailedCallBack;
import com.hcc.config.center.client.context.ConfigContext;
import com.hcc.config.center.client.entity.AppConfigInfo;
import com.hcc.config.center.client.entity.MsgInfo;
import com.hcc.config.center.client.processor.ConfigCenterMsgProcessor;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 客户端，接受服务端消息，动态刷新值
 *
 * @author shengjun.hu
 * @date 2022/10/9
 */
@Slf4j
@Data
public class ConfigCenterClient {

    private ConfigContext configContext;
    private ProcessFailedCallBack callBack;
    private String host;
    private int port;

    private ConfigCenterMsgProcessor configCenterMsgProcessor;
    private NioEventLoopGroup eventLoopGroup;
    private boolean stopFlag = false;

    public ConfigCenterClient(String host, int port, ConfigContext configContext, ProcessFailedCallBack callBack) {
        this.host = host;
        this.port = port;
        this.configContext = configContext;
        this.callBack = callBack;
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
     * 连接服务器
     * @param host
     * @param port
     * @param isReconnect
     */
    private void doConnect(String host, int port, boolean isReconnect) {
        Bootstrap bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup();
        ConfigCenterClient that = this;
        try {
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            configCenterMsgProcessor = new ConfigCenterMsgProcessor(configContext, callBack);
                            pipeline.addLast(new ConfigCenterIdleStateHandler(configContext, that));
                            pipeline.addLast(new ConfigCenterClientHandler(configCenterMsgProcessor));
                        }
                    });
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
                    this.refreshDynamicConfig();
                    log.info("重连后刷新动态配置成功！");
                }
            }
        };
    }

    /**
     * 刷新动态配置
     */
    private void refreshDynamicConfig() {
        List<AppConfigInfo> appConfigInfos = configContext.getDynamicConfigFromConfigCenter();
        if (StringUtils.isEmpty(appConfigInfos)) {
            return;
        }

        this.convertToMsgInfo(appConfigInfos).forEach(configCenterMsgProcessor::addMsgToQueue);
    }

    /**
     * 转换为可处理的消息
     * @param dynamicAppConfigInfos
     * @return
     */
    private List<MsgInfo> convertToMsgInfo(List<AppConfigInfo> dynamicAppConfigInfos) {
        return dynamicAppConfigInfos.stream()
                .map(c -> {
                    MsgInfo msgInfo = new MsgInfo();
                    BeanUtils.copyProperties(c, msgInfo);
                    if (c.getVersion() == 0) {
                        msgInfo.setForceUpdate(true);
                        msgInfo.setMsgType(MsgInfo.MsgType.CONFIG_DELETE.name());
                    } else {
                        msgInfo.setForceUpdate(false);
                        msgInfo.setMsgType(MsgInfo.MsgType.CONFIG_UPDATE.name());
                    }

                    return msgInfo;
                })
                .collect(Collectors.toList());
    }

}
