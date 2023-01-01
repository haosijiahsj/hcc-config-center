package com.hcc.config.center.client.netty;

import com.hcc.config.center.client.context.ConfigContext;
import com.hcc.config.center.client.netty.handler.ConfigCenterClientInboundHandler;
import com.hcc.config.center.client.netty.handler.ConfigCenterClientIdleStateHandler;
import com.hcc.config.center.client.netty.handler.ConfigCenterClientMsgDecoder;
import com.hcc.config.center.client.netty.handler.ConfigCenterClientMsgEncoder;
import com.hcc.config.center.client.processor.ConfigCenterMsgProcessor;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * 通道初始化器
 *
 * @author hushengjun
 * @date 2020-09-07-007
 */
public class ConfigCenterClientChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final ConfigContext configContext;
    private final ConfigCenterMsgProcessor configCenterMsgProcessor;
    private final ConfigCenterClient configCenterClient;

    public ConfigCenterClientChannelInitializer(ConfigContext configContext,
                                                ConfigCenterMsgProcessor configCenterMsgProcessor,
                                                ConfigCenterClient configCenterClient) {
        this.configContext = configContext;
        this.configCenterMsgProcessor = configCenterMsgProcessor;
        this.configCenterClient = configCenterClient;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        // 入站
        pipeline.addLast("msgDecoder", new ConfigCenterClientMsgDecoder());
        pipeline.addLast("reconnectHandler", new ConfigCenterClientIdleStateHandler(configContext, configCenterClient));
        pipeline.addLast("msgProcessHandler", new ConfigCenterClientInboundHandler(configCenterMsgProcessor));

        // 出站
        pipeline.addFirst("msgEncoder", new ConfigCenterClientMsgEncoder());
    }

}
