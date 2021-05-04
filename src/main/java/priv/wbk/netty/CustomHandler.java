package priv.wbk.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

/**
 * Created on 2021/5/4.
 *
 * @author wbk
 * @email 3207264942@qq.com
 *
 * 创建自定义助手类
 * SimpleChannelInboundHandler: 相当于【入站 入境】
 */
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
