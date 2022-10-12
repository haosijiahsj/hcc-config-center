package com.hcc.config.center.server.netty;

import com.hcc.config.center.server.netty.handler.ConfigCenterServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * HttpServerInitializer
 *
 * @author hushengjun
 * @date 2020-09-07-007
 */
public class ConfigCenterServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // 向管道加入处理器
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast("decoder", new StringDecoder());
        pipeline.addLast("encoder", new StringEncoder());
        // 读写心跳处理
        pipeline.addLast(new IdleStateHandler(3, 5, 7, TimeUnit.SECONDS));
        // 业务处理
        pipeline.addLast(new ConfigCenterServerHandler());
    }

}
