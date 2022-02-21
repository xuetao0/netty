package org.xue.netty.base;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.xue.netty.server.NettyServerImpl;

import java.nio.ByteBuffer;

public class Decoder extends LengthFieldBasedFrameDecoder {
    private static final InternalLogger log = InternalLoggerFactory.getInstance(Decoder.class);

    public Decoder() {
        super(16777216, 0, 4, 0, 4);
    }

    @Override
    public Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = null;
        try {
            frame = (ByteBuf) super.decode(ctx, in);
            if (null == frame) {
                return null;
            }

            ByteBuffer byteBuffer = frame.nioBuffer();
            int length = byteBuffer.limit();
            byteBuffer.getInt();
            int bodyLength = length - 4;
            byte[] bytes = new byte[bodyLength];
            byteBuffer.get(bytes);
            String str = new String(bytes);
            return JSON.parseObject(str, NettyCmd.class);
        } catch (Exception e) {
            log.error("decode exception", e);
            String addr = NettyServerImpl.parseChannelRemoteAddr(ctx.channel());
            ctx.channel().close().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    log.error("decode error ! {} ", addr, e);
                }
            });
        } finally {
            if (null != frame) {
                frame.release();
            }
        }

        return null;
    }
}
