package com.vtradex.ehub.jtt.jtt809;

import com.vtradex.ehub.jtt.entity.JT809Constants;
import com.vtradex.ehub.jtt.entity.Message;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.Getter;
import lombok.Setter;

/**
 * 从链路
 * @author liuliwen
 *
 */
@Getter
@Setter
public class Jtt809NettyClient {

	private String ip;
	
	private int port;
	
	public Jtt809NettyClient(String ip,int port) {
		this.ip=ip;
		this.port=port;
	}
	
	private Channel channel; 
	
	public void init() throws InterruptedException {
		int a = 0x5d;
		final ByteBuf delimiter = Unpooled.buffer(1);
		delimiter.writeByte(a);
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                    	ch.pipeline().addLast(new ReadTimeoutHandler(300));//5分钟没有消息断开
                    	ch.pipeline().addLast(new DelimiterBasedFrameDecoder(5000, delimiter));
                    	ch.pipeline().addLast(new Jtt809Decoder());//该处理器将信息转换成message对象
						ch.pipeline().addLast(new Jtt809Handler());
                    }
                });
        channel = bootstrap.connect(ip, port).channel();
    }
	
	public static void sendLoginMessage(Channel channel) {
		  Message message=new Message(JT809Constants.UP_CONNECT_REQ,1001);
	        ByteBuf body=Unpooled.buffer(46);
	        body.writeInt(1234);
	        body.writeBytes("VtJtt879".getBytes());
	        byte[] ipBytesource="169.254.246.63".getBytes();
	        byte[] ipByte = Jtt809Util.rightComplementByte(ipBytesource, 32);
	        body.writeBytes(ipByte);
	        body.writeShort(8000);
	        message.setMsgBody(body);
	        Jtt809Util.sendClientMessage(channel, message);
}
	
	public static void sendLocationMessage(Channel channel) {
	        Message message=new Message(JT809Constants.UP_EXG_MSG,1001);
	        ByteBuf body=Unpooled.buffer( 21+1+2+4);
	        byte[] deviceId="DAHW437".getBytes();
	        byte[] ipByte = Jtt809Util.rightComplementByte(deviceId, 21);
	        body.writeBytes(ipByte);
	        body.writeByte(1);
	        body.writeShort(JT809Constants.UP_EXG_MSG_REAL_LOCATION);
	        body.writeInt(36);
	        body.writeByte(0);
	        body.writeBytes(getdate());
	        body.writeBytes(getTime());
	        body.writeInt(127590000);
	        body.writeInt(37590000);
	        body.writeShort(12);
	        body.writeShort(13);
	        body.writeInt(20000);
	        body.writeShort(270);
	        body.writeShort(2500);
	        body.writeInt(4);
	        body.writeInt(4);
	        
	        message.setMsgBody(body);
	        Jtt809Util.sendClientMessage(channel, message);
	}
	
	private static byte[] getdate() {
		byte[] bb=new byte[4];
		bb[2]=0x07;
		bb[3]=(byte) 0xD9;
		bb[1]=0x01;
		bb[0]=0x01;
		return bb;
	}
	
	private static byte[] getTime() {
		byte[] bb=new byte[3];
		bb[2]=0x07;
		bb[1]=0x01;
		bb[0]=0x01;
		return bb;
	}
	
}
