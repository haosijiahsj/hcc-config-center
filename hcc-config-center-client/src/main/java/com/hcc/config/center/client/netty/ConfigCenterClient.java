package com.hcc.config.center.client.netty;

import com.hcc.config.center.client.context.ConfigCenterContext;
import com.hcc.config.center.client.netty.handler.ConfigCenterClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 客户端，接受服务端消息，动态刷新值
 *
 * @author shengjun.hu
 * @date 2022/10/9
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigCenterClient {

    private ConfigCenterContext configCenterContext;
    private String host;
    private int port;

    /**
     * 启动客户端
     */
    public void startUp() {
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class) // 设置客户端通道实现类
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new ConfigCenterClientHandler(configCenterContext));
                        }
                    });
            // 启动客户端连接到客户端
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            throw new IllegalStateException("动态推送客户端启动失败！", e);
        } finally {
            eventLoopGroup.shutdownGracefully();
        }

    }

}
