package priv.wbk.netty.websocket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * Created on 2021/5/4.
 *
 * @author wbk
 * @email 3207264942@qq.com
 *
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
