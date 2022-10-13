package com.hcc.config.center.client.netty.handler;

import com.hcc.config.center.client.context.ConfigCenterContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * ClientHandler
 *
 * @author hushengjun
 * @date 2020-09-12-012
 */
@Slf4j
@AllArgsConstructor
public class ConfigCenterClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private ConfigCenterContext configCenterContext;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        String msg = this.readByteBuf(byteBuf);
        if (msg == null) {
            return;
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

    }

    private String readByteBuf(ByteBuf byteBuf) {
        if (byteBuf == null) {
            return null;
        }

        byte[] buffer = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(buffer);

        return new String(buffer, CharsetUtil.UTF_8);
    }

}
