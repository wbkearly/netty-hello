# Netty 学习项目 (简单的Netty使用方法)

### 引入依赖

* [访问Maven中央仓库](https://mvnrepository.com/artifact/io.netty/netty-all/)

```xml
<!-- https://mvnrepository.com/artifact/io.netty/netty-all -->
<dependency>
    <groupId>io.netty</groupId>
    <artifactId>netty-all</artifactId>
    <version>4.1.63.Final</version>
</dependency>

```
### 构建hello服务器

*  步骤
   
    1. 构建一对主从线程组
       
    2. 定义服务器启动类（Bootstrap）
    
    3. 为服务器设置Channel 
   
    4. 设置处理从线程池的助手类初始化器(类似拦截器)

    5. 监听启动和关闭服务器
    
```java
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
```

* 每个channel由多个handler共同组成管道（pipeline）

   1. 编写初始化器
   
   ```java
   public class HelloServerInitializer extends ChannelInitializer<SocketChannel> {
   
       @Override
       protected void initChannel(SocketChannel channel) throws Exception {
           // 通过channel获取对应的Pipeline
           ChannelPipeline pipeline = channel.pipeline();
           // 通过管道添加Handler
           // HttpServerCodec是 Netty 自己提供的助手类 可以理解为拦截器
           // 当请求到服务器，需要做解码 响应到客户端 需要做编码
           pipeline.addLast("HttpServerCodec", new HttpServerCodec());
   
           // TODO 添加自定义的助手类 返回 hello netty
           pipeline.addLast("customHandler", null);
       }
   }
   ```
  2. 为ServerBootstrap添加初始化器
   
   ` serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(null);`
