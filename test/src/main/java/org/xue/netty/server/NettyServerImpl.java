package org.xue.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.xue.netty.base.*;

import java.net.InetSocketAddress;
import java.net.SocketAddress;


public class NettyServerImpl extends AbstractNetty implements NettyServer {
    private static final InternalLogger log = InternalLoggerFactory.getInstance(NettyServerImpl.class);
    private final NettyServerConfig serverConfig;
    private final ServerBootstrap bootstrap;
    private final EventLoopGroup bossEventLoopGroup;
    private final EventLoopGroup selectEventLopGroup;
    private DefaultEventExecutorGroup defaultEventExecutorGroup;
    private ChannelHandler serverHandler;
    private ChannelHandler channelManager;
    private ChannelHandler authHandler;
    private Encoder encoder;

    public NettyServerImpl(NettyServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        bootstrap = new ServerBootstrap();
        bossEventLoopGroup = new NioEventLoopGroup(1, new ThreadFactoryImpl("boss_group_%d"));
        selectEventLopGroup = new NioEventLoopGroup(1, new ThreadFactoryImpl("select_group_%d"));

    }

    private void initHandler() {
        serverHandler = new NettyServerHandle();
        channelManager = new ChannelManage(log);
        authHandler = new NettyAuthHandle(log);
        encoder = new Encoder();
    }

    @Override
    public void start() {
        defaultEventExecutorGroup = new DefaultEventExecutorGroup(1, new ThreadFactoryImpl("default_group_%d"));
        initHandler();
        bootstrap.group(bossEventLoopGroup, selectEventLopGroup)
                .channel(NioServerSocketChannel.class)
//                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_SNDBUF, 65535)
                .childOption(ChannelOption.SO_RCVBUF, 65535)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .localAddress(new InetSocketAddress(this.serverConfig.getPort()))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(defaultEventExecutorGroup, "authHandler", authHandler)
                                .addLast(defaultEventExecutorGroup,
                                        encoder,
                                        new Decoder(),
                                        new IdleStateHandler(0, 0, 1200),
                                        channelManager,
                                        serverHandler);
                    }

                    @Override
                    public String toString() {
                        return "my_self_netty_server_channelInitializer";
                    }
                });
        try {
            bootstrap.bind().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    public static String parseChannelRemoteAddr(final Channel channel) {
        if (null == channel) {
            return "";
        }
        SocketAddress remote = channel.remoteAddress();
        final String addr = remote != null ? remote.toString() : "";

        if (addr.length() > 0) {
            int index = addr.lastIndexOf("/");
            if (index >= 0) {
                return addr.substring(index + 1);
            }

            return addr;
        }

        return "";
    }
}
