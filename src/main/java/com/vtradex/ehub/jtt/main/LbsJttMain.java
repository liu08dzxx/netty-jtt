package com.vtradex.ehub.jtt.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vtradex.ehub.jtt.jtt809.Jtt809NettyServer;
import com.vtradex.ehub.jtt.util.PropertiesUtil;

public class LbsJttMain {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LbsJttMain.class);
	
	
    public static void main(String[] args) {
//    	DefaultSdkClient client= bulidDefaultSdkClient();
//		client.start();
//		SdkClientUtil.sdkClient=client;
//		LOGGER.info("注入sdkClient到SdkClientUtil工具类完成");
		Jtt809NettyServer server=new Jtt809NettyServer(PropertiesUtil.getInteger("jtt809.port"));
		server.init();
		
	}
    
 
}
