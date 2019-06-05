import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Date;
import java.util.concurrent.TimeUnit;


public class NettyClient {
    private static final int MAX_RETRY = 5;

    public static void main(String[] args) {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        //负责启动客户端及连接服务端
        Bootstrap bootstrap = new Bootstrap();

        bootstrap
                //指定线程模型
                .group(workerGroup)
                //指定IO模型
                .channel(NioSocketChannel.class)
                //定义连接的业务处理逻辑
                .handler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel socketChannel) throws Exception {

                    }
                });
        //调用connect方法进行连接
        /*bootstrap.connect("juein.im", 80).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("连接成功");
            } else {
                System.out.println("连接失败");
            }
        });*/
        connect(bootstrap, "localhost", 1000, MAX_RETRY);
        //bootstrap 同样有attr() option() 等方法 没有child方法
    }

    //失败重连，并设置
    private static void connect(Bootstrap bootstrap, String host, int port, int retry) {
        bootstrap.connect(host, port).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("连接成功");
            } else if (retry == 0) {
                System.out.println("重连次数用完，放弃连接");
            } else {
                int order = (MAX_RETRY - retry) + 1;
                int delay = 1 << order;
                System.err.println(new Date() + ": 连接失败，第" + order + "次重连...");
                bootstrap.config().group().schedule(() -> connect(bootstrap, host, port, retry - 1), delay, TimeUnit.SECONDS);
            }
        });
    }
}
