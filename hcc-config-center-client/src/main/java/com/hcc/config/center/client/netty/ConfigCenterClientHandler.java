package com.hcc.config.center.client.netty;

import com.hcc.config.center.client.entity.MsgInfo;
import com.hcc.config.center.client.processor.ConfigCenterMsgProcessor;
import com.hcc.config.center.client.utils.JsonUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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

    private final ConfigCenterMsgProcessor configCenterMsgProcessor;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        MsgInfo msgInfo = new MsgInfo();
        msgInfo.setMsgType(MsgInfo.MsgType.INIT.name());
        msgInfo.setAppCode(configCenterMsgProcessor.getAppCode());

        // 上报appCode
        ctx.writeAndFlush(this.convertByteBuf(JsonUtils.toJson(msgInfo)));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        String msg = this.readByteBuf(byteBuf);
        if (msg == null) {
            return;
        }

        log.info("收到推送消息：[{}]", msg);

        MsgInfo msgInfo = JsonUtils.toObject(msg, MsgInfo.class);
        configCenterMsgProcessor.addMsgToQueue(msgInfo);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    }

    private String readByteBuf(ByteBuf byteBuf) {
        if (byteBuf == null) {
            return null;
        }

        byte[] buffer = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(buffer);

        return new String(buffer, CharsetUtil.UTF_8);
    }

    private ByteBuf convertByteBuf(String msg) {
        return Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8);
    }

}
