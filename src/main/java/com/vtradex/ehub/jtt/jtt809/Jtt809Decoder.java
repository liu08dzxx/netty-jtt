package com.vtradex.ehub.jtt.jtt809;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vtradex.ehub.jtt.entity.Message;
import com.vtradex.ehub.jtt.util.CRC16CCITT;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.string.StringDecoder;

public class Jtt809Decoder extends StringDecoder {
	
	private static Logger LOGGER=LoggerFactory.getLogger(Jtt809Decoder.class);
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {

		byte startByte = buffer.readByte();
		/**一 ，判断起始字符，使用netty粘包过滤器到这里，分隔符已经去掉了，只需要判断起始符号即可*/
		if (0x5b!= startByte) {
			return;
		}
		/**二 解义，将0x5a0x01替换成0x5b等*/
		byte[] bytes = new byte[buffer.readableBytes()];
		buffer.readBytes(bytes);
		ByteBuf headFormatedBuffer = Unpooled.buffer(bytes.length);
		Jtt809Util.parseBuffer(bytes, headFormatedBuffer);
		/**三 获取 CRC循环冗余校验的值，*/
		int crcCode=getCRCcode(headFormatedBuffer);
		/**四 读取成message对象,报文体使用ByteBuf封装*/
		Message msg = new Message(headFormatedBuffer);
		if(crcCode!=msg.getCrcCode()) {
			LOGGER.warn("CRC校验没有通过");
			LOGGER.warn("本地计算得到得CRC校验码为{},传入的的为：{}",crcCode,msg.getCrcCode());
			return;
		}
		out.add(msg);
		
	}

	private int getCRCcode(ByteBuf headFormatedBuffer) {
		int msgLength=headFormatedBuffer.getInt(0);
		byte[] bb=new byte[msgLength-4];
		headFormatedBuffer.getBytes(0,bb);
		int crcCode=CRC16CCITT.crc16(bb);
		return crcCode;
	}

	/**
	 * @description 将16进制转换为二进制
	 * @param hexStr
	 * @return
	 */
	public static byte[] parseHexStr2Byte(String hexStr) {
		if (hexStr.length() < 1)
			return null;
		byte[] result = new byte[hexStr.length() / 2];
		for (int i = 0; i < hexStr.length() / 2; i++) {
			int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
			int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
			result[i] = (byte) (high * 16 + low);
		}
		return result;
	}


}
