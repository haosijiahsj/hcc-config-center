package com.hcc.config.center.client.netty.handler;

import com.hcc.config.center.client.context.ConfigCenterContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;

/**
 * ClientHandler
 *
 * @author hushengjun
 * @date 2020-09-12-012
 */
@AllArgsConstructor
public class ConfigCenterClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private ConfigCenterContext configCenterContext;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

    }

}
