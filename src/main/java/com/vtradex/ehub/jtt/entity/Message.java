package com.vtradex.ehub.jtt.entity;

import java.io.Serializable;

import com.vtradex.ehub.jtt.jtt809.Jtt809Util;
import com.vtradex.ehub.jtt.util.CRC16CCITT;
import io.netty.util.ReferenceCountUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
public class Message implements Serializable {
	private static final long serialVersionUID = 4398559115325723920L;

	public static final int MSG_HEAD = 0x5b;
	public static final int MSG_TALL = 0x5d;

	// 报文中除数据体外，固定的数据长度
	public static final int MSG_FIX_LENGTH = 26;

	private static int internalMsgNo = 0;
	private long msgLength;// 长度
	private long encryptFlag = 1;// 报文加密标识
	private int msgGesscenterId;// 下级平台接入码
	private long encryptKey;// 数据加密的密钥
	private int crcCode;
	private int msgId;// 业务数据类型
	private int msgSn;// 报文序列号

	private ByteBuf msgBody;
	private byte[] versionFlag = { 0, 0, 1 };

	// 下行报文标识，值为1时，代表发送的数据；默认为0，代表接收的报文
	// private int downFlag = 0;

	/**
	 * 用于发送时的构造函数
	 * @param msgId
	 */
	public Message(int msgId,int msgGesscenterId) {
		// 下行报文需要填充报文序列号
		synchronized ((Integer) internalMsgNo) {
			if (internalMsgNo == Integer.MAX_VALUE) {
				internalMsgNo = 0;
			}
		}
		this.msgSn = ++internalMsgNo;
		this.msgId = msgId;
		// 报文都是定长的，根据类型不同 26为22的头+2个头尾标识+2CRC码
		switch (msgId) {
		case JT809Constants.UP_CONNECT_RSP:
			msgLength = 5L + 26;
			break;
		case JT809Constants.UP_CONNECT_REQ:
			msgLength = 46L + 26L;
			break;
		case JT809Constants.UP_LINKTEST_RSP:
			msgLength = 26L;
			break;
		case JT809Constants.UP_EXG_MSG:
			msgLength = 90L;
			break;
		case JT809Constants.DOWN_TOTAL_RECV_BACK_MSG:
			msgLength = 46L;
			break;
		case JT809Constants.DOWN_CONNECT_REQ:
			msgLength = 4L;
			break;
		default:
			throw new RuntimeException("还没有配置指定报文类型的数据长度");
		}
		this.msgGesscenterId=msgGesscenterId;
        byte[] versioag = {0,0,1};
        this.versionFlag=versioag;
        this.encryptFlag=0L;
        this.encryptKey=123L;
	}

	
	/**
	 * 用于接收时的构造函数
	 * @param buffer 必须已经读取了头标识，否则下标不正确
	 */
	public Message(ByteBuf buffer) {
		msgLength=buffer.readInt();// 4byte 数据长度
		msgSn=buffer.readInt();// 4byte 报文序列号
		msgId=buffer.readShort();// 2byte 业务数据类型
		msgGesscenterId=buffer.readInt();// 4byte 下级平台接入码
		buffer.readBytes(versionFlag);// 3byte
		encryptFlag=buffer.readUnsignedByte();// 1byte
		encryptKey=buffer.readUnsignedInt();// 4byte

        //如果经过了加密，则解密
        if(encryptFlag==1) {
        	byte[] body=new byte[(int)msgLength-26];
    		buffer.readBytes(body);
        	body=Jtt809Util.encrypt(5, 8, 10, (int)encryptKey, body);
        	msgBody=Unpooled.copiedBuffer(body);
        }else {
        	msgBody=buffer.readBytes((int)msgLength-26);
        }
		crcCode=buffer.readUnsignedShort();// 2byte
		ReferenceCountUtil.release(buffer);
	}
	
	
	/**
	 * 使用服务器端接收通道发送这条消息
	 * @param channel
	 * @param message
	 */
	public void send(ChannelHandlerContext ctx) {
		/** 开始发送 */
		// 写头
		ByteBuf bb = Unpooled.buffer(1);
		bb.writeByte(Message.MSG_HEAD);
		ctx.write(bb);
		// 写中间
		ByteBuf formatBuffer = buildHeadAndBody(this);
		ctx.write(formatBuffer);
		// 写尾
		bb = Unpooled.buffer(1);
		bb.writeByte(Message.MSG_TALL);
		ctx.writeAndFlush(bb);
	}
	
	/**
	 * 使用客户端通道发送这条消息
	 * @param channel
	 * @param message
	 */
	public void send(Channel channel) {
		/** 开始发送 */
		// 写头
		ByteBuf bb = Unpooled.buffer(1);
		bb.writeByte(Message.MSG_HEAD);
		channel.write(bb);
		// 写中间
		ByteBuf formatBuffer = buildHeadAndBody(this);
		channel.write(formatBuffer);
		// 写尾
		bb = Unpooled.buffer(1);
		bb.writeByte(Message.MSG_TALL);
		channel.writeAndFlush(bb);	
	}

	/**
	 * 发消息时构建消息头和消息体和CRC码的ByteBuf对象
	 * 
	 * @param message
	 * @return
	 */
	public static ByteBuf buildHeadAndBody(Message message) {
		ByteBuf byteBuf = Unpooled.buffer((int) message.getMsgLength() -4);
		byteBuf.writeInt((int) message.getMsgLength());// 4byte 数据长度
		byteBuf.writeInt(message.getMsgSn());// 4byte 报文序列号
		byteBuf.writeShort(message.getMsgId());// 2byte 业务数据类型
		byteBuf.writeInt(message.getMsgGesscenterId());// 4byte 下级平台接入码
		byteBuf.writeBytes(message.getVersionFlag());// 3byte版本
		byteBuf.writeByte((int) message.getEncryptFlag());// 1byte
		byteBuf.writeInt((int) message.getEncryptKey());// 4byte
		if(message.getMsgBody()!=null) {
			// 判断是否需要加密
			if (message.getEncryptFlag() == 1) {
				ByteBuf bytebody = message.getMsgBody();
				byte[] body = bytebody.array();
				body = Jtt809Util.encrypt(5, 8, 10, (int) message.getEncryptKey(), body);
				byteBuf.writeBytes(body);
			} else {
				byteBuf.writeBytes(message.getMsgBody());
			}
		}
		// 计算CRC
		int a = CRC16CCITT.crc16(byteBuf.array());
		// 写CRC
		byteBuf.writeShort(a);
		// 转义,不知道转义后的具体长度
		ByteBuf formatBuffer = Unpooled.buffer();
		Jtt809Util.formatBuffer(byteBuf.array(), formatBuffer);
		byteBuf.release();
		return formatBuffer;
	}

}
