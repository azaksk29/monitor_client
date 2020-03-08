package com.monitor_client.app.netty_client;

import com.monitor_client.app.define.ClientDataType;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Getter
public class NettyClient {

	private String host;

	private int port;

	private int srcPort;

	private int clientDataType;

	private Channel channel;

	public static Map<Integer,Channel> channelMap = new HashMap();

	public NettyClient(int clientDataType, String host, int port) {
		this.clientDataType = clientDataType;
		this.host = host;
		this.port = port;
	}

	public NettyClient(int clientDataType, String host, int port, int srcPort) {
		this.clientDataType = clientDataType;
		this.host = host;
		this.port = port;
		this.srcPort = srcPort;
	}

	public void doConnect() {
		EventLoopGroup group = new NioEventLoopGroup();

		final StringDecoder DECODER = new StringDecoder();
		final StringEncoder ENCODER = new StringEncoder();

		Bootstrap b = new Bootstrap();

		try {
			b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							ChannelPipeline p = ch.pipeline();
							//p.addLast(DECODER);
							//p.addLast(ENCODER);
							p.addLast(new NettyClientHandler());
						}
					});
		} catch (Exception e) {
			e.printStackTrace();
		}

		ChannelFuture cf = b.connect(getHost(), getPort());

		cf.addListener((ChannelFutureListener) futureListener -> {
			if (futureListener.isSuccess()) {
				Channel channel = futureListener.channel();
				if(clientDataType == ClientDataType.DATA_TYPE_CAPTURED_PACKET)
					channelMap.put(srcPort,channel);
				this.channel = channel;
				log.info("Connect to server successfully!-> host:port:{}", host + ":" + port);
			} else {
				log.info("Failed to connect to server, try connect after 5s-> host:port:{}",host + ":" + port);
				futureListener.channel().eventLoop().schedule(this::doConnect, 5, TimeUnit.SECONDS);
			}
		});
// TODO : shutdown
//		finally {
//			if(channel == null)
//				group.shutdownGracefully();
//		}
	}

	public void send(ByteBuf dataBuf) {
		Channel channel = getChannel();
		if(channel != null) {
			if(channel.isActive()) {
				channel.writeAndFlush(dataBuf);
			}
			else {
				log.error("channel is no active ...");
			}
		}
		else {
			log.error("channel is disconnected ...");
		}
	}

	public static void send(Channel channel, ByteBuf dataBuf) {
		if(channel != null) {
			if(channel.isActive()) {
				channel.writeAndFlush(dataBuf);
			}
			else {
				log.error("channel is no active ...");
			}
		}
		else {
			log.error("channel is disconnected ...");
		}
	}
}