package com.vtradex.ehub.jtt.entity;

public class JT809Constants {
	/**
	 * 主链路登录请求消息
	 */
	public static final int UP_CONNECT_REQ = 0x1001;
	/**
	 * 主链路登录应答消息
	 */
	public static final int UP_CONNECT_RSP = 0x1002;
	
	/**
	 * 登录成功
	 */
	public static final int UP_CONNECT_RSP_SUCCESS = 0x00;
	/**
	 * IP 地址不正确
	 */
	public static final int UP_CONNECT_RSP_ERROR_01 = 0x01;
	/**
	 * 接入码不正确
	 */
	public static final int UP_CONNECT_RSP_ERROR_02 = 0x02;
	/**
	 *  用户没注册
	 */
	public static final int UP_CONNECT_RSP_ERROR_03 = 0x03;//
	/**
	 * 密码错误
	 */
	public static final int UP_CONNECT_RSP_ERROR_04 = 0x04;
	/**
	 * 资源紧张,稍后再连接(已经占 用);
	 */
	public static final int UP_CONNECT_RSP_ERROR_05 = 0x05;// 
	/**
	 * 其他
	 */
	public static final int UP_CONNECT_RSP_ERROR_06 = 0x06;

	public static int UP_DICONNECE_REQ = 0x1003;// 主链路注销请求消息

	public static int UP_DISCONNECT_RSP = 0x1004;// 主链路注销应答消息

	/**
	 * 主链路连接保持请求消息
	 */
	public static final int UP_LINKETEST_REQ = 0x1005;

	/**
	 * 主链路连接保持应答消息
	 */
	public static final int UP_LINKTEST_RSP = 0x1006;

	public static int UP_DISCONNECT_INFORM = 0x1007;// 主链路断开通知消息

	public static int UP_CLOSELINK_INFORM = 0x1008;// 下级平台主动关闭链路通 知消息

	/**
	 * 从链路连接请求消息
	 */
	public final static int DOWN_CONNECT_REQ = 0x9001;
	
	

	/**
	 * 从链路连接应答消息
	 */
	public final static int DOWN_CONNECT_RSP = 0x9002;

	/**
	 * 连接成功
	 */
	public final static int DOWN_CONNECT_RSP_SUCCESS=0x00;
	/**
	 * VERIFY_CODY错误
	 */
	public final static int DOWN_CONNECT_RSP_ERROR_1=0x01;
	/**
	 * 资源紧张，稍后再连接
	 */
	public final static int DOWN_CONNECT_RSP_ERROR_2=0x02;
	/**
	 * 其他
	 */
	public final static int DOWN_CONNECT_RSP_ERROR_3=0x03;
	
	
	public static int DOWN_DISCONNECT_REQ = 0x9003;// 从链路注销请求消息

	/**
	 * 主链路车辆动态信息交换消息
	 */
	public static final int UP_EXG_MSG = 0x1200;
	/**
	 * 实时上传车辆定位信息
	 */
	public static final int UP_EXG_MSG_REAL_LOCATION = 0x1202;
	/**
	 * 车辆定位信息自动补报
	 */
	public static final int UP_EXG_MSG_HISTORY_LOCATION = 0x1203;

	/**
	 * 车辆定位信息自动补报响应
	 */
	public static final int DOWN_TOTAL_RECV_BACK_MSG=0x9101;
	
	public static int DOWN_EXG_MSG = 0x9200;// 从链路动态信息交换消息

}
