package org.xue.netty.client;

import io.netty.channel.Channel;

import java.util.concurrent.atomic.AtomicBoolean;

public class ClientChannel {
    private Channel channel;
    private AtomicBoolean okStatus = new AtomicBoolean(false);
    private final NettyClient nettyClient;

    public ClientChannel(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
    }


    public boolean isOk() {
        return okStatus.get();
    }

    public Channel getChannel() {
        if (okStatus.get()) {
            return channel;
        } else {
            nettyClient.connect();
            return null;
        }
    }

    public void setOkStatus(boolean status) {
        okStatus.set(status);
    }

    public void setChannel(Channel channel) {
        okStatus.set(true);
        this.channel = channel;
    }
}
