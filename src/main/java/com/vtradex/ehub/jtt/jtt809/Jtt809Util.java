package com.vtradex.ehub.jtt.jtt809;

import java.nio.charset.Charset;
import java.util.Date;

import org.joda.time.DateTime;

import com.vtradex.ehub.jtt.entity.LoginRequest;
import com.vtradex.ehub.jtt.entity.LoginResponse;
import com.vtradex.ehub.jtt.entity.Message;
import com.vtradex.ehub.jtt.util.CRC16CCITT;
import com.vtradex.ehub.jtt.util.DecimalConversion;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

public class Jtt809Util {
	/**
	 * 报文转义 * void * @param bytes * @param formatBuffer
	 */
	public static void formatBuffer(byte[] bytes, ByteBuf formatBuffer) {
		for (byte b : bytes) {
			switch (b) {
			case 0x5b:
				byte[] formatByte0x5b = new byte[2];
				formatByte0x5b[0] = 0x5a;
				formatByte0x5b[1] = 0x01;
				formatBuffer.writeBytes(formatByte0x5b);
				break;
			case 0x5a:
				byte[] formatByte0x5a = new byte[2];
				formatByte0x5a[0] = 0x5a;
				formatByte0x5a[1] = 0x02;
				formatBuffer.writeBytes(formatByte0x5a);
				break;
			case 0x5d:
				byte[] formatByte0x5d = new byte[2];
				formatByte0x5d[0] = 0x5e;
				formatByte0x5d[1] = 0x01;
				formatBuffer.writeBytes(formatByte0x5d);
				break;
			case 0x5e:
				byte[] formatByte0x5e = new byte[2];
				formatByte0x5e[0] = 0x5e;
				formatByte0x5e[1] = 0x02;
				formatBuffer.writeBytes(formatByte0x5e);
				break;
			default:
				formatBuffer.writeByte(b);
				break;
			}
		}
	}

	/**
	 * 报文解义 * void * @param bytes * @param formatBuffer
	 */
	public static void parseBuffer(byte[] bytes, ByteBuf formatBuffer) {
		byte lastByte = 0x00;
		boolean conver = false;
		for (byte b : bytes) {
			// 如果出现0x5a或0x5e就缓存，否则就直接写入
			if (b == 0x5a || b == 0x5e) {
				lastByte = b;
				conver = true;
				continue;
			}
			// 如果上一次有缓存，则判断是否紧跟0x01和0x02，如果是，就一起写入，如果不是则将上一次和本次的分别写入,缓存标记置为空
			if (conver) {
				if (b == 0x01) {
					if (lastByte == 0x5a) {
						formatBuffer.writeByte(0x5b);
						conver = false;
						continue;
					}
					if (lastByte == 0x5e) {
						formatBuffer.writeByte(0x5d);
						conver = false;
						continue;
					}
				}
				if (b == 0x02) {
					if (lastByte == 0x5a) {
						formatBuffer.writeByte(0x5a);
						conver = false;
						continue;
					}
					if (lastByte == 0x5e) {
						formatBuffer.writeByte(0x5e);
						conver = false;
						continue;
					}
				}
				formatBuffer.writeByte(lastByte);
				conver = false;
			}
			// 如果没出现0x5a和0x5e,也没有缓存标记，则直接写入
			formatBuffer.writeByte(b);
		}
	}

	/**
	 * 加密
	 * 
	 * @param M1
	 * @param IA1
	 * @param IC1
	 * @param key
	 * @param data
	 * @return
	 */
	public static byte[] encrypt(int M1, int IA1, int IC1, int key, byte[] data) {
		if (data == null) {
			return null;
		}
		byte[] array = data;// 使用原对象，返回原对象
		// byte[] array = new byte[data.length]; //数组复制 返回新的对象
		// System.arraycopy(data, 0, array, 0, data.length);
		int idx = 0;
		if (key == 0) {
			key = 1;
		}
		int mkey = M1;
		if (0 == mkey) {
			mkey = 1;
		}
		while (idx < array.length) {
			key = IA1 * (key % mkey) + IC1;
			array[idx] ^= ((key >> 20) & 0xFF);
			idx++;
		}
		return array;
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
		return formatBuffer;
	}

	/**
	 * 发送一条客户端消息
	 * 
	 * @param channel
	 * @param message
	 */
	public static void sendClientMessage(Channel channel, Message message) {
		/** 开始发送 */
		// 写头
		ByteBuf bb = Unpooled.buffer(1);
		bb.writeByte(Message.MSG_HEAD);
		channel.write(bb);
		// 写中间
		ByteBuf formatBuffer = buildHeadAndBody(message);
		channel.write(formatBuffer);
		// 写尾
		bb = Unpooled.buffer(1);
		bb.writeByte(Message.MSG_TALL);
		channel.writeAndFlush(bb);
	}

	/**
	 * 服务端发送一条响应消息
	 * 
	 * @param channel
	 * @param message
	 */
	public static void sendServerMessage(ChannelHandlerContext ctx, Message message) {
		/** 开始发送 */
		// 写头
		ByteBuf bb = Unpooled.buffer(1);
		bb.writeByte(Message.MSG_HEAD);
		ctx.write(bb);
		// 写中间
		ByteBuf formatBuffer = buildHeadAndBody(message);
		ctx.write(formatBuffer);
		// 写尾
		bb = Unpooled.buffer(1);
		bb.writeByte(Message.MSG_TALL);
		ctx.writeAndFlush(bb);
		
	}

	/**
	 * 转换登陆报文
	 */
	public static LoginRequest convertLoginRequest(Message message) {
		ByteBuf buffer = message.getMsgBody();
		if (message.getEncryptFlag() == 1) {
			// 报文体解密
		}
		LoginRequest request = new LoginRequest();
		int userId = buffer.readInt();// 4位整型用户名
		request.setUserId(userId);
		String passWord = buffer.readBytes(8).toString(Charset.forName("GBK"));
		request.setPassword(passWord);
		String ip = buffer.readBytes(32).toString(Charset.forName("GBK"));
		request.setDownLinkIp(ip.trim());
		int port = buffer.readUnsignedShort();// 从链路服务端口号
		request.setDownLinkPort(port);
		return request;
	}

	public static LoginResponse convertLoginResponse(Message message) {
		ByteBuf buffer = message.getMsgBody();
		LoginResponse request = new LoginResponse();
		int result = buffer.readByte();
		request.setResult(result);
		int verifyCode = buffer.readInt();
		request.setVerifyCode(verifyCode);
		return request;
	}

	/**
	 * 转换byte[],日月年，为标准的yyyyMMdd,byte[]中年的表示是先将年转换成两位十六进制数，如2009表示为 0x07 0xD9
	 * Hms,时分秒
	 * 
	 * @param date
	 * @return
	 */
	public static Date builderDate(byte[] dMy, byte[] hms) {
		int day = dMy[0];
		int month = dMy[1];
		// 年份转换
		byte[] by = new byte[] { dMy[2], dMy[3] };
		String a = DecimalConversion.bytesToHexString(by);
		// 将十六进制转化成十进制
		int year = Integer.parseInt(a, 16);
		int hour = hms[0];
		int minute = hms[1];
		int sencond = hms[2];
		DateTime time = new DateTime(year, month, day, hour, minute, sencond);
		return time.toDate();
	}

	
	/**
	 * 不定长字符串右补0x00，需要去掉再转
	 * @param readBytes
	 * @return
	 */
	public static String trimString(ByteBuf bytes) {
		int length=bytes.readableBytes();
		byte[] sourceByte=new byte[length];
		byte[] goalByte=null;
		bytes.readBytes(sourceByte);
		for(int i=length-1;i>-1;i--) {
			if(goalByte==null) {
				if(sourceByte[i]!=0x00) {
					goalByte=new byte[i+1];
					goalByte[i]=sourceByte[i];
				}
			}else {
				goalByte[i]=sourceByte[i];
			}
		}
		ReferenceCountUtil.release(bytes);
		bytes.release();
		return new String(goalByte,Charset.forName("GBK"));
	}
	
	/**
	 * 不够指定长度的字符数组，右补0x00补齐位数
	 * @param ipByte
	 * @return
	 */
	public static byte[] rightComplementByte(byte[] ipByte,int length) {
		byte[] goalByte=new byte[length];
		for(int i=0;i<length;i++) {
			if(i<ipByte.length) {
				goalByte[i]=ipByte[i];
			}else {
				goalByte[i]=0x00;
			}
		}
		return goalByte;
	}

}
