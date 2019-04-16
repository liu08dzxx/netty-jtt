package com.vtradex.ehub.jtt.jtt809;

public enum LoginStatusEnum {
    
	/**
	 * 初始化状态
	 */
	init,
	/**
	 * 等待登陆，超时未登陆则断开
	 */
	waiting,
	/**
	 * 登陆成功
	 */
	runnable;
	
	
	LoginStatusEnum(){
		
	}
}
