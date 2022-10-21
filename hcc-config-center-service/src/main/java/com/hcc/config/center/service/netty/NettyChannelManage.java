package com.hcc.config.center.service.netty;

import com.hcc.config.center.domain.vo.PushConfigClientMsgVo;
import com.hcc.config.center.service.utils.JsonUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NettyChannelManage
 *
 * @author shengjun.hu
 * @date 2022/10/12
 */
@Slf4j
public class NettyChannelManage {

    private static final String CLIENT_ID = "CLIENT_ID";

    private static final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final Map<String, AppChannel> clientIdChannelMap = new ConcurrentHashMap<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class AppChannel {
        private String clientId;
        private String appCode;
        private String ip;
        private Channel channel;
    }

    private static String getClientId(Channel channel) {
        AttributeKey<String> key = AttributeKey.valueOf(CLIENT_ID);
        return channel.attr(key).get();
    }

    public synchronized static void addChannel(Channel channel) {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        channel.attr(AttributeKey.valueOf(CLIENT_ID)).setIfAbsent(uuid);

        channelGroup.add(channel);

        log.info("客户端ip: {}连接到服务器！", channel.remoteAddress());
    }

    public synchronized static void removeChannel(Channel channel) {
        String clientId = getClientId(channel);
        channelGroup.remove(channel);
        clientIdChannelMap.remove(clientId);

        log.info("客户端：{}，ip: {}离开！", clientId, channel.remoteAddress());
    }

    public synchronized static void addAppChannelRelation(String appCode, Channel channel) {
        String clientId = appCode + "_" + getClientId(channel);
        AppChannel appChannel = new AppChannel();
        appChannel.setClientId(clientId);
        appChannel.setAppCode(appCode);
        appChannel.setIp(channel.remoteAddress().toString());
        appChannel.setChannel(channel);

        clientIdChannelMap.put(clientId, appChannel);

        log.info("客户端：{}，appCode: {}初始化成功！", clientId, appCode);
    }

    private static ByteBuf convertByteBuf(String msg) {
        return Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8);
    }

    public synchronized static void sendMsg(String clientId, String msg) {
        AppChannel appChannel = clientIdChannelMap.get(clientId);
        if (appChannel == null) {
            return;
        }
        appChannel.getChannel().writeAndFlush(convertByteBuf(msg));

        log.info("客户端：{}，消息: {}发送成功！", clientId, msg);
    }

    public synchronized static void sendMsgToApp(String appCode, String msg) {
        for (Map.Entry<String, AppChannel> entry : clientIdChannelMap.entrySet()) {
            AppChannel v = entry.getValue();
            if (!v.getAppCode().equals(appCode)) {
                continue;
            }
            v.getChannel().writeAndFlush(convertByteBuf(msg));

            log.info("客户端：{}，消息: {}发送成功！", v.getClientId(), msg);
        }
    }

    /**
     * 当前实例是否存在appCode的客户端
     * @param appCode
     * @return
     */
    public static boolean existAppCodeClient(String appCode) {
        for (Map.Entry<String, AppChannel> entry : clientIdChannelMap.entrySet()) {
            AppChannel v = entry.getValue();
            if (v.getAppCode().equals(appCode)) {
                return true;
            }
        }

        return false;
    }

    public synchronized static void sendMsgToApp(String appCode, PushConfigClientMsgVo msg) {
        for (Map.Entry<String, AppChannel> entry : clientIdChannelMap.entrySet()) {
            AppChannel v = entry.getValue();
            if (!v.getAppCode().equals(appCode)) {
                continue;
            }
            msg.setClientId(v.getClientId());
            v.getChannel().writeAndFlush(convertByteBuf(JsonUtils.toJson(msg)));

            log.info("客户端：{}，消息: {}发送成功！", v.getClientId(), msg);
        }
    }

    public synchronized static void sendMsgToAll(String msg) {
        channelGroup.writeAndFlush(convertByteBuf(msg));

        log.info("所有客户端，消息: {}发送成功！", msg);
    }

}
