package com.hcc.config.center.server.netty;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
    private static final Map<String, List<Channel>> appCodeChannelsMap = new ConcurrentHashMap<>();

    public synchronized static void addChannel(Channel channel) {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        channel.attr(AttributeKey.valueOf(clientId)).setIfAbsent(uuid);

        channelGroup.add(channel);
    }

    public synchronized static void removeChannel(Channel channel) {
        channelGroup.remove(channel);

        AttributeKey<String> key = AttributeKey.valueOf(clientId);
        String clientId = channel.attr(key).get();
        if (clientId == null) {
            return;
        }

        for (Map.Entry<String, List<Channel>> entry : appCodeChannelsMap.entrySet()) {
            String appCode = entry.getKey();
            List<Channel> channels = entry.getValue();

            boolean flag = false;
            List<Channel> newChannels = new ArrayList<>();
            for (Channel tempChannel : channels) {
                String tempClientId = tempChannel.attr(key).get();
                if (clientId.equals(tempClientId)) {
                    flag = true;
                    continue;
                }
                newChannels.add(tempChannel);
            }
            if (flag) {
                appCodeChannelsMap.put(appCode, newChannels);
                break;
            }
        }
    }

    public synchronized static void addAppChannelRelation(String appCode, Channel channel) {
        List<Channel> channels = appCodeChannelsMap.get(appCode);
        if (channels == null) {
            channels = new ArrayList<>();
        }
        channels.add(channel);
        appCodeChannelsMap.put(appCode, channels);
    }

    public synchronized static void sendMsg(String appCode, String msg) {
        List<Channel> channels = appCodeChannelsMap.get(appCode);
        if (CollectionUtils.isEmpty(channels)) {
            log.info("应用：{}没有客户端订阅，不发送消息！", appCode);
            return;
        }
        channels.forEach(channel -> {
            channel.writeAndFlush(msg);
        });
    }

    public synchronized static void sendMsgToAll(String msg) {
        channelGroup.writeAndFlush(msg);
    }

}
