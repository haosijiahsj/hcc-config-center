package com.hcc.config.center.server.netty.handler;

import com.hcc.config.center.domain.vo.PushConfigClientMsgVo;
import com.hcc.config.center.service.utils.JsonUtils;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.CharsetUtil;

import java.nio.CharBuffer;
import java.util.List;

/**
 * 消息编码器
 *
 * @author hushengjun
 * @date 2023/1/1
 */
public class ConfigCenterServerEncoder extends MessageToMessageEncoder<PushConfigClientMsgVo> {

    @Override
    protected void encode(ChannelHandlerContext ctx, PushConfigClientMsgVo msgVo, List<Object> out) throws Exception {
        if (msgVo == null) {
            return;
        }
        String jsonMsg = JsonUtils.toJson(msgVo);

        out.add(ByteBufUtil.encodeString(ctx.alloc(), CharBuffer.wrap(jsonMsg), CharsetUtil.UTF_8));
    }

}
