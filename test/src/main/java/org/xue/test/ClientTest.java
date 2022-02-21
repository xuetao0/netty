package org.xue.test;


import org.xue.netty.base.NettyCmd;
import org.xue.netty.client.NettyClient;
import org.xue.netty.client.NettyClientConfig;
import org.xue.netty.client.NettyClientImpl;

import java.io.IOException;


public class ClientTest {
    public static void main(String[] args) {
        NettyClientConfig clientConfig = new NettyClientConfig();
        clientConfig.setServerAddr("192.168.3.41:9988");
        NettyClient client = new NettyClientImpl(clientConfig);
        client.start();
        client.connect();
        while (true) {
            int read = 0;
            try {
                read = System.in.read();
            } catch (IOException e) {

            }
            sendMessage(client);
        }
    }

    public static void sendMessage(NettyClient client) {
        NettyCmd cmd = new NettyCmd();
        cmd.setCode(222);
        cmd.setBody("asdsadasdasd");
        cmd.setDesc("说得轻巧");
        client.sendMsg(cmd);
    }

}
