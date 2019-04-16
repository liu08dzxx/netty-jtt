package com.vtradex.ehub.jtt.entity;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vtradex.ehub.jtt.jtt809.Jtt809Util;
import com.vtradex.ehub.jtt.util.DecimalConversion;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * 车辆动态信息交换
 * 
 * @author liuliwen
 *
 */
@Getter
@Setter
@RequiredArgsConstructor
public abstract class Vehicle {

	public static Logger LOGGER = LoggerFactory.getLogger(Vehicle.class);
	

	
	
	
	
	
	/**
	 * 构建车辆动态信息交换对象
	 * @param msgBody
	 * @return
	 */
	public static Optional<Vehicle> builderVehicle(ByteBuf msgBody,int msgGesscenterId) {
		Vehicle request=null;
		// 按顺序为21,1,2,4位
	    //处理车牌byte数组，去掉右补的0x00;
		String vehicleNo= Jtt809Util.trimString(msgBody.readBytes(21));
//		String vehicleNo = msgBody.readBytes(21).toString(Charset.forName("GBK"));
		byte vehicleColor = msgBody.readByte();
		int dataType = msgBody.readUnsignedShort();
		int dataLength=msgBody.readInt();//必须读4位数据长度，移动下标，否则接下来的读取都是错误的
		switch (dataType) {
		case JT809Constants.UP_EXG_MSG_REAL_LOCATION:
			request = new VehicleCurrentLocation(vehicleNo, vehicleColor, msgBody);
			break;
		case JT809Constants.UP_EXG_MSG_HISTORY_LOCATION:
			request = new VehicleHistoryLocation(vehicleNo, vehicleColor,dataLength,msgBody);
			break;	
		default:
			LOGGER.warn("未处理的交换数据子业务类型 :"+DecimalConversion.intToHex(dataType));
			return Optional.ofNullable(null);
		}
		ReferenceCountUtil.release(msgBody);
		request.setMsgGesscenterId(msgGesscenterId);
		return Optional.ofNullable(request);
	}


	/**
	 * 业务处理，动态车辆定位信息上传，或车辆注册，或补传定位的特殊处理,
	 * @return 如果是定位上传，返回定位上传的数量
	 */
	public abstract int handlerBiz(String orgKey);

	
	/**
	 * 车牌号
	 */
	protected String vehicleNo;
	/**
	 * 颜色
	 */
	protected byte vehicleColor;
	/**
	 * 交换数据类型
	 */
	protected int dataType;
	/**
	 * 后续数据长度
	 */
	protected int dataLength;
	
	protected int msgGesscenterId;

}
