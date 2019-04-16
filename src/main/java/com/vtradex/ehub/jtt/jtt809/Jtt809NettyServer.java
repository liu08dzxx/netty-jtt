package com.vtradex.ehub.jtt.jtt809;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.vtradex.ehub.jtt.entity.Message;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Jtt809NettyServer {
	
	public static Logger LOGGER=LoggerFactory.getLogger(Jtt809NettyServer.class);
	
	public Jtt809NettyServer(int port) {
		this.port=port;
	}
	//主链路的端口
	private int port;
	/**
	 * 对应的从链路,key为msgGesscenterId
	 */
	private HashMap<Integer,Jtt809NettyClient> clientMap=Maps.newHashMap();
	/**
	 * 主链路登陆状态
	 */
	private LoginStatusEnum loginStatus = LoginStatusEnum.init;
	
	
	public void init() {
		final Jtt809NettyServer server=this;
		//粘包分隔符
		final ByteBuf delimiter = Unpooled.buffer(1);
		delimiter.writeByte(Message.MSG_TALL);
		ServerBootstrap serverBootstrap = new ServerBootstrap();
		NioEventLoopGroup boos = new NioEventLoopGroup();
		NioEventLoopGroup worker = new NioEventLoopGroup();
		serverBootstrap.group(boos, worker).channel(NioServerSocketChannel.class);
		serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
					protected void initChannel(NioSocketChannel ch) {
						ch.pipeline().addLast(new ReadTimeoutHandler(300));//5分钟没有消息断开
//						ch.pipeline().addLast(new IdleStateHandler(300,300,300));//5分钟没有消息断开
						ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1000, delimiter));
						ch.pipeline().addLast(new Jtt809Decoder());//该处理器将信息转换成message对象
						ch.pipeline().addLast(new Jtt809Handler(server));
					}
				}).bind(port);//32965
	}

}
