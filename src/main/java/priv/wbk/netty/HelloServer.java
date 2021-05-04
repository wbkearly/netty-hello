package priv.wbk.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Created on 2021/5/4.
 *
 * @author wbk
 * @email 3207264942@qq.com
 *
 * 实现客户端发送请求 服务端返回 hello netty
 */
public class HelloServer {

    public static void main(String[] args) throws InterruptedException {
        // 定义一对线程组
        // 主线程组，用于接收客户端链接，不做任何处理
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // 从线程组，做任务
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // Netty 服务器的创建 ServerBootstrap是一个启动类
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            // 设置主从线程组
            serverBootstrap.group(bossGroup, workerGroup)
                    // 设置NIO的双向通道
                    .channel(NioServerSocketChannel.class)
                    // 子处理器 TODO
                    .childHandler(null);
            // 启动Server 设置8088为启动的端口号 启动方式为同步
            ChannelFuture channelFuture = serverBootstrap.bind(8088)
                    .sync();
            // 监听关闭的Channel 设置同步方式
            channelFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
