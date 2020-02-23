package com.monitor_client.app.netty_client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.Getter;

@Getter
public class NettyClient {

	private String host;

	private int port;

	private ChannelFuture cf;

	public NettyClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void start() {
		EventLoopGroup group = new NioEventLoopGroup();

		final StringDecoder DECODER = new StringDecoder();
		final StringEncoder ENCODER = new StringEncoder();

		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							ChannelPipeline p = ch.pipeline();
							p.addLast(DECODER);
							p.addLast(ENCODER);
							p.addLast(new NettyClientHandler());
						}
					});

			cf = b.connect(getHost(), getPort()).sync();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// group.shutdownGracefully();
		}
	}

	public void sendByteBuf(ByteBuf dataBuf) {
		Channel ch = getCf().channel();
		System.out.println("send >>>");
		ch.writeAndFlush(dataBuf);
	}
}