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
  
   `serverBootstrap.xxxx.xxxx.childHandler(new HelloServerInitializer());`

    3. 编写自定义助手类

    ```java
    public class CustomHandler extends SimpleChannelInboundHandler<HttpObject> {

        /**
         * 从缓冲区读数据
         */
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
             if (msg instanceof HttpRequest) {
                // 定义发送的数据消息
                ByteBuf content = Unpooled.copiedBuffer("hello netty", CharsetUtil.UTF_8);
                // 构建一个HttpResponse
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
                // 为响应增加数据类型和长度
                response.headers()
                        .set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
                response.headers()
                        .set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
                // 把响应刷到客户端
                ctx.writeAndFlush(response);
            }
        }
    }

    ```
  
    4. 添加自定义助手类
    
    `pipeline.addLast("customHandler", new CustomHandler());`

### 使用Netty构建websocket服务器

* 服务器编写与hello服务器类似

    1. 初始化器
    
    ```java
    public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {
    
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            // ------------------支持HTTP协议------------------
            // Websocket 基于HTTP 协议, 要有http编解码器
            pipeline.addLast(new HttpServerCodec());
            // 对 写大数据流的支持
            pipeline.addLast(new ChunkedWriteHandler());
            // 对HttpMessage进行聚合 成FullHttpRequest或FullHttpResponse
            // 几乎在netty编程 都会使用到
            pipeline.addLast(new HttpObjectAggregator(1024 *64));
    
            // websocket 服务器处理的协议，用于指定给客户端连接访问的路由: /ws
            // 处理握手动作
            // 对于websocket来讲 都是以frames来传输 不同数据类型对应得frame也不同
            pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
            // 自定义的助手类
            pipeline.addLast(new ChatHandler());
        }
    }
    ```
  
    2. 自定义助手类
    
    ```java
    /**
     * 处理消息的Handler
     * TextWebSocketFrame 为websocket专门处理文本的对象 frame 是消息载体
     */
    @Slf4j
    public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    
        /**
         * 用于记录和管理所有客户端的channel
         */
        private static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
          // 客户端传输过来的消息
          String content = msg.text();
          log.info("接收的数据: {}", content);
          // 针对Channel发送消息
          clients.writeAndFlush(new TextWebSocketFrame(
          "[服务器]在" + LocalDateTime.now()  + "接收到消息, 消息为:" + content));
        }
        
        
        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            clients.add(ctx.channel());
        
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            // 当触发handlerRemoved, ChannelGroup 会自动移除对应客户端的channel
            log.info("客户端断开，Channel Long Id: {}", ctx.channel().id().asLongText());
            log.info("客户端断开，Channel Short Id: {}", ctx.channel().id().asShortText());
            log.info("建立连接的客户端数量为: {}", clients.size());
        }
    }

    ```