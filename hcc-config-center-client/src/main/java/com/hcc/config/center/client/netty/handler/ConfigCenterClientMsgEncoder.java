package com.hcc.config.center.client.netty.handler;

import com.hcc.config.center.client.entity.SendServerMsg;
import com.hcc.config.center.client.utils.JsonUtils;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.CharBuffer;
import java.util.List;

/**
 * 消息encoder
 *
 * @author hushengjun
 * @date 2023/1/1
 */
@Slf4j
public class ConfigCenterClientMsgEncoder extends MessageToMessageEncoder<SendServerMsg> {

    @Override
    protected void encode(ChannelHandlerContext ctx, SendServerMsg msg, List<Object> out) throws Exception {
        if (msg == null) {
            return;
        }

        String jsonMsg = JsonUtils.toJson(msg);
        log.info("发送消息：[{}]", jsonMsg);

        out.add(ByteBufUtil.encodeString(ctx.alloc(), CharBuffer.wrap(jsonMsg), CharsetUtil.UTF_8));
    }

}
