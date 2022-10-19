package com.hcc.config.center.server.netty;

import com.hcc.config.center.service.utils.ApplicationContextUtils;
import com.hcc.config.center.service.zk.ZkHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * ConfigCenterServer
 *
 * @author shengjun.hu
 * @date 2022/10/11
 */
@Slf4j
@AllArgsConstructor
public class ConfigCenterServer {

    private final String host;
    private final int port;

    public void startUp() {
        log.info("开始启动动态推送服务");

        EventLoopGroup bossEventLoopGroup = new NioEventLoopGroup();
        EventLoopGroup workerEventLoopGroup = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        try {
            serverBootstrap.group(bossEventLoopGroup, workerEventLoopGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ConfigCenterServerInitializer());

            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            channelFuture.addListener(future -> {
                if (channelFuture.isSuccess()) {
                    // 注册到zk
                    ZkHandler zkHandler = ApplicationContextUtils.getBean(ZkHandler.class);
                    zkHandler.registerServerNode(host, port);
                    log.info("绑定端口：[{}]成功！启动完成！", port);
                } else {
                    log.info("绑定端口：[{}]失败！", port);
                }
            });
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            throw new IllegalStateException("动态推送服务启动失败！", e);
        } finally {
            bossEventLoopGroup.shutdownGracefully();
            workerEventLoopGroup.shutdownGracefully();
        }
    }

}
