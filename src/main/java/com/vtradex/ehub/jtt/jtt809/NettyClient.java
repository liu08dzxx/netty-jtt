package com.vtradex.ehub.jtt.jtt809;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.vtradex.ehub.jtt.entity.JT809Constants;
import com.vtradex.ehub.jtt.entity.LoginResponse;
import com.vtradex.ehub.jtt.entity.Message;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

public class NettyClient {
	
	
	
    
	public static void main(String[] args) throws InterruptedException {
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
                    	ch.pipeline().addLast(new DelimiterBasedFrameDecoder(5000, delimiter));
                    	ch.pipeline().addLast(new Jtt809Decoder());//该处理器将信息转换成message对象
                    	ch.pipeline().addLast(new SimpleChannelInboundHandler<Message>() {
							@Override
							protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
								System.out.println("接收到一条" + msg.toString());
								if(msg.getMsgId()==JT809Constants.UP_CONNECT_RSP) {
									LoginResponse response=Jtt809Util.convertLoginResponse(msg);
									System.out.println("接收到登陆响应报文："+JSON.toJSONString(response));
								}
							}
                    		
                    	});
                    }
                });
//
        Channel channel = bootstrap.connect("110.53.222.19", 32965).channel();
//        Channel channel = bootstrap.connect("10.60.28.58", 7090).channel();
        
        //循环到连接成功
        while(!channel.isActive()) {
        	TimeUnit.SECONDS.sleep(1);
        	continue;
        }
        
        //发送一条登陆消息
        
        TimeUnit.SECONDS.sleep(2);
        sendLoginMessage(channel);
        //循环发送定位
        TimeUnit.SECONDS.sleep(2);
        long allstartTime=System.currentTimeMillis();
        long startTime=System.currentTimeMillis();
        int i=0;
        while(true) {
            sendLocationMessage(channel);
            i++;
            if(i%100000==0) {
            	long endTime=System.currentTimeMillis();
            	System.out.println("发送100000个定位,总数:"+i+"，用时 "+(endTime-startTime));
            	startTime=endTime;
            }
            if(i==1) {
            	System.out.println("发送200000000个定位，用时："+(System.currentTimeMillis()-allstartTime));
            	break;
            }
            while(!channel.isWritable()) {
            	TimeUnit.MICROSECONDS.sleep(50);
            	continue;
            }
        }

//        message.send(channel);
      //循环到连接被断开
        while(channel.isActive()) {
        	TimeUnit.SECONDS.sleep(1);
        	continue;
        }
        System.out.println("连接被断开：时间："+new Date());
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
