package org.xue.netty.base;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class NettyServerHandle extends SimpleChannelInboundHandler<NettyCmd> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NettyCmd msg) throws Exception {
        System.out.println(msg);
    }
}