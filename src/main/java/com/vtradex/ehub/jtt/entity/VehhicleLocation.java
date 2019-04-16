package com.vtradex.ehub.jtt.entity;

import java.util.Date;

import com.vtradex.ehub.jtt.jtt809.Jtt809Util;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class VehhicleLocation {
	
	/**
	 * (encrypt=24, date=[3, 4, 7, -29], time=[15, 35, 34], lon=114458465, 
     * lat=37596030, vec1=0, vec2=0, vec3=5118, direction=4, altitude=10752, state=786435, alarm=0);
	 * 读取子业务报文部分
	 * @param byteBuf
	 */
	public VehhicleLocation(ByteBuf byteBuf) {
		encrypt=byteBuf.readByte();
		byteBuf.readBytes(date);
		byteBuf.readBytes(time);
		lon=byteBuf.readInt();
		lat=byteBuf.readInt();
		vec1=byteBuf.readUnsignedShort();
		vec2=byteBuf.readUnsignedShort();
		vec3=byteBuf.readInt();
		direction=byteBuf.readUnsignedShort();
		altitude=byteBuf.readUnsignedShort();
		state=byteBuf.readInt();
		alarm=byteBuf.readInt();
		byteBuf.release();
	}

	/**
	 * 转换成lbs定位上传请求对象
	 * @return
	 */
	public LbsUploadLocationRequest convertLocation(String vehicleNo) {
		LbsUploadLocationRequest location=new LbsUploadLocationRequest();
		location.setDeviceId(vehicleNo);
		if(encrypt==1) {
			Vehicle.LOGGER.info("定位数据有加密，但还没有做解密处理");
		}
		Date locationTime=Jtt809Util.builderDate(date,time);
		location.setLocationTimeDate(locationTime);
		double longitude=lon/1000000;
		location.setLongitude(longitude+"");
		double latitude=lat/1000000;
		location.setLatitude(latitude+"");
		return location;
	}
	
	private byte encrypt;
	/**
	  * dmyy,年的表示是先将年转换成两位十六进制数，如2009表示为 0x07 0xD9
	  */
	private byte[] date=new byte[4];
	    
	    /**
	     * 时分秒
	     */
	private byte[] time=new byte[3];
		/**
		 * 经度 单位为1*10的负6次方度
		 */
	private int lon;
		
	private int lat;
		
		/**
		 * 卫星监测到的速度，单位km/h
		 */
	private int vec1;
		/**
		 * 行车设备记录到的速度，单位km/h
		 */
		
	private int vec2;
		/**
		 * 车辆当前总里程数，km
		 */
	private int vec3;
		/**
		 * 方向 ，0-359单位为度，正北为0，顺时针
		 */
	private int direction;
		
		/**
		 * 海拔高度，单位为米
		 */
	private int altitude;
		/**
		 * 车辆状态，二进制表示，具体看文档
		 */
	private int state;
		
		/**
		 * 报警状态，二进制表示，0表示正常，1表示报警，具体看文档
		 */
	private int alarm;
}
