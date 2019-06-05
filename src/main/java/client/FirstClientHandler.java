package client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.Charset;
import java.util.Date;

public class FirstClientHandler extends ChannelInboundHandlerAdapter {

    //覆盖了该方法
    //客户端连接建立成功后，调用到channelActive()方法 我们在该方法中编写向服务端写数据的逻辑
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println(new Date() + ": 客户端写出数据");

        //1. 获取数据
        ByteBuf buffer = getByteBuf(ctx);

        //2. 写数据
        ctx.channel().writeAndFlush(buffer);
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;
        System.out.println(new Date() + ":客户端读到数据 -> " + byteBuf.toString(Charset.forName("utf-8")));
    }

    private ByteBuf getByteBuf(ChannelHandlerContext ctx) {
        //1. 获取二进制抽象ByteBuf
        ByteBuf buffer = ctx.alloc().buffer();

        //2. 准备数据 指定字符串的字符集为 utf-8
        byte[] bytes = "你好，服务器".getBytes(Charset.forName("utf-8"));

        //3. 填充数据到 ByteBuf
        buffer.writeBytes(bytes);

        return buffer;
    }
}
