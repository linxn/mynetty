package server;

import client.FirstClientHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class NettyServer {
    public static void main(String[] args) {
        //两大线程组 bossGroup表示连接线程组 workerGroup表示数据读写线程组
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        final AttributeKey<Object> serverKey = AttributeKey.newInstance("serverKey");
        final AttributeKey<Object> clientKey = AttributeKey.newInstance("clientKey");

        //引导服务端的启动
        final ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap
                //给引导类指定线程模型
                .group(bossGroup, workerGroup)
                //指定服务端的IO模型为Nio （还有其他的BIO等）
                .channel(NioServerSocketChannel.class)
                //attr用于给NioServerSocketChannel指定一些自定义属性  然后可以通过channel.attr()可以取出这个属性
                .attr(serverKey, "nettyServer")
                //给连接自定义属性
                .childAttr(clientKey, "clientValue")
                //给每条连接设置TCP底层相关属性
                //SO_KEEPALIVE表示是否开启TCP底层心跳机制  TCP_NODELAY表示是否开启Nagle算法（实时性要求不高，减少发送次数）
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                //给服务端设置属性  SO_BACKLOG表示系统用于临时存放已完成的三次握手的请求队列的最大长度 如果建立连接频繁 服务器处理新连接满 则可以适当调大参数
                .option(ChannelOption.SO_BACKLOG, 1024)
                //handler用于指定服务端启动过程中的一些逻辑
                .handler(new ChannelInitializer<NioServerSocketChannel>() {
                    protected void initChannel(NioServerSocketChannel nioServerSocketChannel) throws Exception {
                        System.out.println("服务端启动中");
                        System.out.println(nioServerSocketChannel.attr(serverKey).get());
                    }
                })
                //定义每条连接的数据读写  业务处理逻辑 （注意child）
                //NioServerSocketChannel NioSocketChannel可以和ServerSocket Socket对应上
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        //当有连接进入的时候才会print
                        System.out.println(nioSocketChannel.attr(clientKey).get());

                        //读取来自客户端的业务逻辑 重点业务逻辑在FirstServerhandler
                        nioSocketChannel.pipeline().addLast(new FirstServerHandler());
                    }
                });


        //bind方法返回值为ChannelFuture 可以添加一个监听器GenericFutureListener 用opertionComplete方法判断是否绑定成功
        /*serverBootstrap.bind(8000).addListener(new GenericFutureListener<Future<? super Void>>() {
            public void operationComplete(Future<? super Void> future) throws Exception {
                if (future.isSuccess()) {
                    System.out.println("端口绑定成功");
                } else {
                    System.out.println("端口绑定失败");
                }
            }
        });*/
        bind(serverBootstrap, 1000);

    }
    //如果端口被占用等原因绑定失败 则端口号 +1 继续尝试绑定
    private static void bind(final ServerBootstrap serverBootstrap, final int port) {
        serverBootstrap.bind(port).addListener(new GenericFutureListener<Future<? super Void>>() {
            public void operationComplete(Future<? super Void> future) throws Exception {
                if (future.isSuccess()) {
                    System.out.println("端口[" + port + "]绑定成功");
                } else {
                    System.out.println("端口[" + port + "]绑定失败");
                    bind(serverBootstrap, port+1);
                }
            }
        });
    }
}
