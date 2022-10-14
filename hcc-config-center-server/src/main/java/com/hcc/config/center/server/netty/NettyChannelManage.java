package com.hcc.config.center.server.netty;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
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

    private static final String clientId = "CLIENT_ID";

    private static final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final Map<String, AppChannel> clientIdChannelMap = new ConcurrentHashMap<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class AppChannel {
        private String clientId;
        private String appCode;
        private Channel channel;
    }

    private static String getClientId(Channel channel) {
        AttributeKey<String> key = AttributeKey.valueOf(clientId);
        return channel.attr(key).get();
    }

    public synchronized static void addChannel(Channel channel) {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        channel.attr(AttributeKey.valueOf(clientId)).setIfAbsent(uuid);

        channelGroup.add(channel);
    }

    public synchronized static void removeChannel(Channel channel) {
        channelGroup.remove(channel);
        clientIdChannelMap.remove(getClientId(channel));
    }

    public synchronized static void addAppChannelRelation(String appCode, Channel channel) {
        String clientId = getClientId(channel);
        AppChannel appChannel = new AppChannel();
        appChannel.setClientId(clientId);
        appChannel.setAppCode(appCode);
        appChannel.setChannel(channel);

        clientIdChannelMap.put(clientId, appChannel);
    }

    public synchronized static void sendMsg(String clientId, String msg) {
        AppChannel appChannel = clientIdChannelMap.get(clientId);
        if (appChannel == null) {
            return;
        }
        appChannel.getChannel().writeAndFlush(msg);
    }

    public synchronized static void sendMsgToApp(String appCode, String msg) {
        for (Map.Entry<String, AppChannel> entry : clientIdChannelMap.entrySet()) {
            AppChannel v = entry.getValue();
            if (!v.getAppCode().equals(appCode)) {
                continue;
            }
            v.getChannel().writeAndFlush(msg);
        }
    }

    public synchronized static void sendMsgToAll(String msg) {
        channelGroup.writeAndFlush(msg);
    }

}
