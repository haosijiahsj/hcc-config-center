package com.hcc.config.center.client.netty.handler;

import com.hcc.config.center.client.entity.MsgEventType;
import com.hcc.config.center.client.entity.ReceivedServerMsg;
import com.hcc.config.center.client.entity.SendServerMsg;
import com.hcc.config.center.client.processor.ConfigCenterMsgProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 消息入站处理器
 *
 * @author hushengjun
 * @date 2020-09-12-012
 */
@Slf4j
@AllArgsConstructor
public class ConfigCenterClientInboundHandler extends SimpleChannelInboundHandler<ReceivedServerMsg> {

    private final ConfigCenterMsgProcessor configCenterMsgProcessor;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        SendServerMsg sendServerMsg = new SendServerMsg();
        sendServerMsg.setMsgType(MsgEventType.INIT.name());
        sendServerMsg.setAppCode(configCenterMsgProcessor.getAppCode());

        // 上报appCode
        ctx.writeAndFlush(sendServerMsg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ReceivedServerMsg msg) throws Exception {
        if (msg == null) {
            return;
        }
        configCenterMsgProcessor.addMsgToQueue(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
    }

}
