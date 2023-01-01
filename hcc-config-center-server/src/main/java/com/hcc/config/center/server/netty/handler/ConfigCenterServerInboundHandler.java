package com.hcc.config.center.server.netty.handler;

import com.hcc.config.center.service.context.ReceivedClientMsgInfo;
import com.hcc.config.center.service.context.NettyChannelContext;
import com.hcc.config.center.service.utils.JsonUtils;
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
public class ConfigCenterServerInboundHandler extends SimpleChannelInboundHandler<String> {

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        NettyChannelContext.addChannel(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String jsonMsg) throws Exception {
        ReceivedClientMsgInfo receivedClientMsgInfo = JsonUtils.toObject(jsonMsg, ReceivedClientMsgInfo.class);
        if (ReceivedClientMsgInfo.MsgType.INIT.name().equals(receivedClientMsgInfo.getMsgType())) {
            NettyChannelContext.addAppChannelRelation(receivedClientMsgInfo.getAppCode(), ctx.channel());
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        NettyChannelContext.removeChannel(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        NettyChannelManage.removeChannel(ctx.channel());
        log.error("客户端：{}，连接异常关闭！", ctx.channel().remoteAddress());
        ctx.channel().close();
    }

}
