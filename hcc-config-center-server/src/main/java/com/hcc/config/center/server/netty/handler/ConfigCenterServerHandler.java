package com.hcc.config.center.server.netty.handler;

import com.hcc.config.center.server.netty.NettyChannelManage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * ConfigCenterServerHandler
 *
 * @author shengjun.hu
 * @date 2022/10/12
 */
@Slf4j
public class ConfigCenterServerHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        NettyChannelManage.addChannel(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        // TODO
        NettyChannelManage.addAppChannelRelation(null, ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        NettyChannelManage.removeChannel(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        NettyChannelManage.removeChannel(ctx.channel());
    }

}
