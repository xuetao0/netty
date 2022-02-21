package org.xue.test;

import org.xue.netty.server.NettyServer;
import org.xue.netty.server.NettyServerConfig;
import org.xue.netty.server.NettyServerImpl;

public class ServerTest {
    public static void main(String[] args) {
        NettyServerConfig serverConfig = new NettyServerConfig();
        serverConfig.setPort(9988);
        NettyServer nettyServer = new NettyServerImpl(serverConfig);
        nettyServer.start();
    }
}
