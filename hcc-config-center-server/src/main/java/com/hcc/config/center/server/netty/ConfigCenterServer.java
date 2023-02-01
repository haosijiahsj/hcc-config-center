package com.hcc.config.center.server.netty;

import com.hcc.config.center.service.utils.ApplicationContextUtils;
import com.hcc.config.center.service.zk.ZkHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * ConfigCenterServer
 *
 * @author shengjun.hu
 * @date 2022/10/11
 */
@Slf4j
public class ConfigCenterServer {

    private final String host;
    private final int port;

    private int bossThreads = 0;
    private int workerThreads = 0;

    private EventLoopGroup bossEventLoopGroup;
    private EventLoopGroup workerEventLoopGroup;

    public ConfigCenterServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void setBossThreads(Integer bossThreads) {
        this.bossThreads = bossThreads == null || bossThreads < 0 ? 0 : bossThreads;
    }

    public void setWorkerThreads(Integer workerThreads) {
        this.workerThreads = workerThreads == null || workerThreads < 0 ? 0 : workerThreads;
    }

    public void startUp() {
        log.info("开始启动动态推送服务");

        // 默认机器核数 * 2
        bossEventLoopGroup = new NioEventLoopGroup(bossThreads);
        workerEventLoopGroup = new NioEventLoopGroup(workerThreads);

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        try {
            serverBootstrap.group(bossEventLoopGroup, workerEventLoopGroup)
                    // 服务端使用的NioServerSocketChannel
                    .channel(NioServerSocketChannel.class)
                    // 设置全连接队列的大小
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ConfigCenterServerChannelInitializer());

            // 绑定端口，并同步处理
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            channelFuture.addListener(this.channelFutureListener());
            // 对通道关闭监听
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            throw new IllegalStateException("动态推送服务启动失败！", e);
        } finally {
            bossEventLoopGroup.shutdownGracefully();
            workerEventLoopGroup.shutdownGracefully();
        }
    }

    /**
     * 关闭netty服务
     */
    public void stop() {
        if (bossEventLoopGroup != null) {
            bossEventLoopGroup.shutdownGracefully();
        }
        if (workerEventLoopGroup != null) {
            workerEventLoopGroup.shutdownGracefully();
        }
        log.info("推送服务器关闭！");
    }

    /**
     * ChannelFuture监听器
     * @return
     */
    private ChannelFutureListener channelFutureListener() {
        return future -> {
            if (future.isSuccess()) {
                // 注册到zk
                ZkHandler zkHandler = ApplicationContextUtils.getBean(ZkHandler.class);
                zkHandler.registerServerNode(host, port);
                log.info("绑定端口：[{}]成功！启动完成！", port);
            } else {
                log.info("绑定端口：[{}]失败！", port);
            }
        };
    }

}
