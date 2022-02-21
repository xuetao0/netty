package org.xue.netty.base;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.logging.InternalLogger;


import java.nio.ByteBuffer;

@ChannelHandler.Sharable
public class NettyAuthHandle extends SimpleChannelInboundHandler<ByteBuf> {
    private final InternalLogger log;

    public NettyAuthHandle(InternalLogger log) {
        this.log = log;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        msg.markReaderIndex();
        ByteBuffer buf = msg.nioBuffer();
        byte[] bytes = new byte[buf.limit()];
        buf.get(bytes);
        String str = new String(bytes);
        log.info("auth handler: {}", str);
        msg.resetReaderIndex();
        ctx.pipeline().remove(this);
        ctx.fireChannelRead(msg.retain());
    }
}