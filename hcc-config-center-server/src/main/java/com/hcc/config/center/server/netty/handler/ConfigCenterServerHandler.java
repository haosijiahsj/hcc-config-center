package com.hcc.config.center.server.netty.handler;

import com.hcc.config.center.service.netty.NettyChannelManage;
import com.hcc.config.center.service.netty.MsgInfo;
import com.hcc.config.center.service.utils.JsonUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
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
        String json = this.readByteBuf(msg);
        MsgInfo msgInfo = JsonUtils.toObject(json, MsgInfo.class);
        if (MsgInfo.MsgType.INIT.getCode().equals(msgInfo.getMsgType())) {
            NettyChannelManage.addAppChannelRelation(msgInfo.getAppCode(), ctx.channel());
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        NettyChannelManage.removeChannel(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        NettyChannelManage.removeChannel(ctx.channel());
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
