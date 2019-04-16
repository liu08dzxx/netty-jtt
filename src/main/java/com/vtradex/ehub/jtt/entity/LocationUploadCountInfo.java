package com.vtradex.ehub.jtt.entity;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LocationUploadCountInfo {
	
	public LocationUploadCountInfo(int msgGesscenterId) {
		this.msgGesscenterId=msgGesscenterId;
	}
	
	public int addLocation(int num) {
		countTotal.addAndGet(num);
		
		return count.addAndGet(num);
	}

	
	/**
	 * 唯一码
	 */
	private int msgGesscenterId;
	
	/**
	 * 初始化时间
	 */
	private Date initDate=new Date();
	
	/**
	 * 从初始化时间开始的总数累计
	 */
	private AtomicInteger countTotal=new AtomicInteger(0);
	/**
	 * 上次统计开始时间
	 */
    private Date startTime=new Date();
    
    /**
	 * 统计数量
	 */
    private AtomicInteger count=new AtomicInteger(0);
}
