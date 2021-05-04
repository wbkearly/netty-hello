package priv.wbk.netty.websocket;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * Created on 2021/5/4.
 *
 * @author wbk
 * @email 3207264942@qq.com
 */
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
