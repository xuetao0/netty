package org.xue.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.xue.netty.base.*;

import java.util.concurrent.*;


public class NettyClientImpl extends AbstractNetty implements NettyClient {
    private static final InternalLogger log = InternalLoggerFactory.getInstance(NettyClientImpl.class);
    private final NettyClientConfig clientConfig;
    private DefaultEventExecutorGroup defaultEventExecutorGroup;
    private final EventLoopGroup eventLoopGroupWorker;
    private final Bootstrap bootstrap = new Bootstrap();
    private final ClientChannel clientChannel = new ClientChannel(this);

    public NettyClientImpl(NettyClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        this.eventLoopGroupWorker = new NioEventLoopGroup(1, new ThreadFactoryImpl("work_event_loop_group_%d"));
    }


    @Override
    public void start() {
        this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(1, new ThreadFactoryImpl("default_event_loop_group_%d"));
        this.bootstrap.group(this.eventLoopGroupWorker).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .option(ChannelOption.SO_SNDBUF, 65535)
                .option(ChannelOption.SO_RCVBUF, 65535)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(defaultEventExecutorGroup,
                                new Encoder(),
                                new Decoder(),
                                new IdleStateHandler(0, 0, 1200),
                                new ChannelManage(log),
                                new NettyClientHandle());
                    }
                });

    }

    @Override
    public void connect() {
        if (!clientChannel.isOk()) {
            log.info("will connect channel");
            ChannelFuture cf = this.bootstrap.connect(Tool.string2SocketAddress(clientConfig.getServerAddr()));
            if (cf.awaitUninterruptibly(3000)) {
                if (isOK(cf)) {
                    log.info("createChannel: connect remote host[{}] success, {}", clientConfig.getServerAddr(), cf.toString());
                    clientChannel.setChannel(cf.channel());
                } else {
                    log.warn("createChannel: connect remote host[" + clientConfig.getServerAddr() + "] failed, " + cf.toString(), cf.cause());
                }
            } else {
                log.warn("createChannel: connect remote host[{}] timeout {}ms, {}", clientConfig.getServerAddr(), 3000,
                        cf.toString());
            }
        }
    }


    private final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(10);
    private final ExecutorService msgExecutor = new ThreadPoolExecutor(1, 1, 1, TimeUnit.HOURS, queue, new ThreadFactoryImpl("msg_executor_%d"));

    @Override
    public void sendMsg(NettyCmd cmd) {

        Runnable run = new Runnable() {
            @Override
            public void run() {
                log.info("start send msg ");
                Channel channel = clientChannel.getChannel();
                CountDownLatch lc = new CountDownLatch(1);
                if (channel != null) {
                    log.info("check  send msg channel");
                    if (channel.isActive()) {
                        log.info("will  send msg ");
                        channel.writeAndFlush(cmd).addListener(new ChannelFutureListener() {
                            @Override
                            public void operationComplete(ChannelFuture future)  {
                                if (future.isSuccess()) {
                                    log.info("send msg success ");
                                    log.info("over send result is {} flag is {}", true, this.hashCode());
                                    lc.countDown();
                                    return;
                                }
                                log.info("send msg fail ");
                                lc.countDown();
                            }
                        });
                        try {
                            lc.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        channel.close().addListener(new ChannelFutureListener() {
                            @Override
                            public void operationComplete(ChannelFuture future) throws Exception {
                                log.error("channel close success,will connect ");
                                clientChannel.setOkStatus(false);
                            }
                        });
                        log.error("channel is un active !" + channel.hashCode());
                    }
                } else {
                    long l = System.currentTimeMillis();
                    log.error("channel is null!" + l);
                }
            }
        };
        msgExecutor.execute(run);
        log.info("over send  flag is {}", "over :" + run.hashCode());
    }


    public boolean isOK(ChannelFuture cf) {
        return cf.channel() != null && cf.channel().isActive();
    }
}
