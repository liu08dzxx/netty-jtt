package com.vtradex.ehub.jtt.jtt809;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.vtradex.ehub.jtt.entity.JT809Constants;
import com.vtradex.ehub.jtt.entity.LocationUploadCountInfo;
import com.vtradex.ehub.jtt.entity.Message;
import com.vtradex.ehub.jtt.util.ByteBufPool;
import com.vtradex.ehub.jtt.util.DateUtil;

import io.netty.buffer.ByteBuf;

/**
 * 定位统计工具
 * @author liuliwen
 *
 */
public class LocationUploadCountUtil {
	
	public static Logger LOGGER = LoggerFactory.getLogger(LocationUploadCountUtil.class);
	/**
	 * 定位数量统计，按量发送响应消息
	 */
	public static Map<Integer,LocationUploadCountInfo> LOCATION_COUNT_MAP=Maps.newConcurrentMap();

	/**
	 * 统计定位数量，如果单次统计大于指定数量则返回一个响应消息对象
	 * @param num
	 */
	public static Optional<Message> addLocationNum(int msgGesscenterId,int num,int sendNum) {
		if(num<1) {
			return Optional.empty();
		}
		LocationUploadCountInfo info;
		if(LOCATION_COUNT_MAP.containsKey(msgGesscenterId)) {
			info=LOCATION_COUNT_MAP.get(msgGesscenterId);
		}else {
			synchronized (msgGesscenterId+"") {
				if(LOCATION_COUNT_MAP.containsKey(msgGesscenterId)) {
					info=LOCATION_COUNT_MAP.get(msgGesscenterId);
				}else {
					info=new LocationUploadCountInfo(msgGesscenterId);
					LOCATION_COUNT_MAP.put(msgGesscenterId, info);
				}
			}
		}

		int count=info.addLocation(num);
		//数量每大于100发送一次响应报文，并重置单次统计的值
		if(count>sendNum) {
			synchronized (info) {
				count=info.getCount().get();
				if(count>sendNum) {
					LOGGER.info("累计接收到msgGesscenterId为{}的定位数量：{},开始时间：{} ",msgGesscenterId,info.getCountTotal().get(),DateUtil.SIMPLE_FORMAT.format(info.getInitDate()));
					//发送响应
					Message response=new Message(JT809Constants.DOWN_TOTAL_RECV_BACK_MSG,msgGesscenterId);
			        ByteBuf body=ByteBufPool.BYTE_BUF_POOL.buffer(20);
			        //写入4位数量
			        body.writeInt(count);
			        Date now=new Date();
			        //写入开始结束时间
			        body.writeLong(DateUtil.convertUTC(info.getStartTime(), DateUtil.SIMPLE_FORMAT).getTime()/1000);
			        body.writeLong(DateUtil.convertUTC(now, DateUtil.SIMPLE_FORMAT).getTime()/1000);
			        response.setMsgBody(body);
			        //主链路得到的报文才使用从链路发送，从链路直接自己发送
			        
			        info.getCount().set(0);
			        info.setStartTime(now);
			        return Optional.of(response);
				}
			}
		}
		return Optional.empty();
	}
}
