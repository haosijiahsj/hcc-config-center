package com.hcc.config.center.client.netty.handler;

import com.hcc.config.center.client.entity.ReceivedServerMsg;
import com.hcc.config.center.client.utils.JsonUtils;
import com.hcc.config.center.client.utils.StrUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 消息decode实现
 *
 * @author hushengjun
 * @date 2023/1/1
 */
@Slf4j
public class ConfigCenterClientMsgDecoder extends MessageToMessageDecoder<ByteBuf> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        String jsonMsg = msg.toString(CharsetUtil.UTF_8);
        if (StrUtils.isEmpty(jsonMsg)) {
            return;
        }

        log.info("收到推送消息：[{}]", jsonMsg);

        try {
            ReceivedServerMsg receivedServerMsg = JsonUtils.toObject(jsonMsg, ReceivedServerMsg.class);
            out.add(receivedServerMsg);
        } catch (Exception e) {
            log.error("消息非法", e);
        }
    }

}
