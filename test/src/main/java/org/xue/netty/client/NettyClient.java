package org.xue.netty.client;

import org.xue.netty.base.NettyCmd;

public interface NettyClient {
    void start();

    void connect();

    void sendMsg(NettyCmd cmd);


}
