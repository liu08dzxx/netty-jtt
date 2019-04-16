package com.vtradex.ehub.jtt.entity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;



/**
 * 
 * 上传定位信息请求
 * 
 * @author liuliwen
 */
public class LbsUploadLocationRequest  {
	private static SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	/**
	 * 设备号
	 */
	private String deviceId;

	/**
	 * 经度
	 */
	private String longitude;
	
	/**拼接设备，有该字段时，将该字段拼接到设备ID上*/
	private String xxx;
	
	/**
	 * 纬度
	 */
	private String latitude;
	
	private String sourceLongitude;
	
	private String sourceLatitude;
	/**
	 * 定位时间
	 */
	private String locationTime;
	private Date locationTimeDate;
	

	private String deviceType;
	/**
	 * 2016-06-20新增字段 城市
	 */
	private String city;
	/**
	 * 2016-06-20新增字段 省份
	 */
	private String province;
	/**
	 * 2016-06-20新增字段 地址
	 */
	private String position;

	/**
	 * 温度
	 */
	private String[] temperatures;
	
	/**
	 * 湿度
	 */
	private String[] humidity;

	/**
	 * 开门预警 ["1","x","0"]
	 */
	private String[] doorsensorStatus;
	
	/**
	 * 密码
	 */
	private String doorsensorPassword;

	private String operatorName;


	public String getDoorsensorPassword() {
		return doorsensorPassword;
	}

	public void setDoorsensorPassword(String doorsensorPassword) {
		this.doorsensorPassword = doorsensorPassword;
	}


	public void setLocationTimeDate(Date locationTimeDate) {
		this.locationTimeDate = locationTimeDate;
		this.locationTime=format.format(locationTimeDate);

	}

	public String getXxx() {
		return xxx;
	}

	public void setXxx(String xxx) {
		this.xxx = xxx;
	}

	public String[] getHumidity() {
		return humidity;
	}

	public void setHumidity(String[] humidity) {
		this.humidity = humidity;
	}

	public String[] getTemperatures() {
		return temperatures;
	}

	public void setTemperatures(String[] temperatures) {
		this.temperatures = temperatures;
	}

	public Date getLocationTimeDate() {
		return locationTimeDate;
	}

	public String getSourceLongitude() {
		return sourceLongitude;
	}

	public void setSourceLongitude(String sourceLongitude) {
		this.sourceLongitude = sourceLongitude;
	}

	public String getSourceLatitude() {
		return sourceLatitude;
	}

	public void setSourceLatitude(String sourceLatitude) {
		this.sourceLatitude = sourceLatitude;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public String[] getDoorsensorStatus() {
		return doorsensorStatus;
	}

	public void setDoorsensorStatus(String[] doorsensorStatus) {
		this.doorsensorStatus = doorsensorStatus;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLocationTime() {
		return locationTime;
	}

	public void setLocationTime(String locationTime) {
		this.locationTime = locationTime;
		try {
			this.locationTimeDate=format.parse(locationTime);
		} catch (ParseException e) {
			throw new RuntimeException("定位时间格式错误转换失败");
		}
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getOperatorName() {
		return operatorName;
	}

	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}


	
}
