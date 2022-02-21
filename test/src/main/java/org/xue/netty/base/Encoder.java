package org.xue.netty.base;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@ChannelHandler.Sharable
public class Encoder extends MessageToByteEncoder<NettyCmd> {
    @Override
    protected void encode(ChannelHandlerContext ctx, NettyCmd msg, ByteBuf out) throws Exception {

        String jsonString = JSON.toJSONString(msg);
        byte[] bytes = jsonString.getBytes(StandardCharsets.UTF_8);
        ByteBuffer header = getHeader(bytes.length);
        out.writeBytes(header);
        out.writeBytes(bytes);
    }

    private ByteBuffer getHeader(int bodyLength) {
        // 1> header length size
        int length = 4;
        // 2> header data length
        // 3> body data length
        length += bodyLength;

        ByteBuffer result = ByteBuffer.allocate(8);
        // length
        result.putInt(length);
        // header length
        result.put(markProtocolType(0));
        result.flip();
        return result;
    }

    public static byte[] markProtocolType(int source) {
        byte[] result = new byte[4];

        result[0] = (byte) 0;
        result[1] = (byte) ((source >> 16) & 0xFF);
        result[2] = (byte) ((source >> 8) & 0xFF);
        result[3] = (byte) (source & 0xFF);
        return result;
    }
}
