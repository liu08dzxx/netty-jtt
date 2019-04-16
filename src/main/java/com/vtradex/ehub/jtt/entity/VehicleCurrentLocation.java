package com.vtradex.ehub.jtt.entity;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 车辆实时定位
 * @author liuliwen
 *
 */
@Getter
@Setter
@RequiredArgsConstructor
@ToString
public class VehicleCurrentLocation extends Vehicle{
	
	/**
	 * 构造VehicleCurrentLocation对象，这里的msgBody的下标必须已经移动到该对象对应的数据部分
	 * @param vehicleNo
	 * @param vehicleColor
	 * @param msgBody
	 */
	public  VehicleCurrentLocation(String vehicleNo,byte vehicleColor,ByteBuf msgBody) {
		this.vehicleNo=vehicleNo;
		this.vehicleColor=vehicleColor;
		this.dataType=JT809Constants.UP_EXG_MSG_REAL_LOCATION;
		this.dataLength=36;
		location=new VehhicleLocation(msgBody);
		
	}
	

	/**
	 * 定位上传
	 */
	@Override
	public int handlerBiz(String orgKey) {
		List<LbsUploadLocationRequest> list=Lists.newArrayList();
		LbsUploadLocationRequest request=location.convertLocation(vehicleNo);
		list.add(request);
		LOGGER.info("进行报文转换后，调用sdk方法上传定位，{}",JSON.toJSONString(request));
		return 1;
	}
	
	/**
	 * 定位
	 */
	private VehhicleLocation location;
	
}
