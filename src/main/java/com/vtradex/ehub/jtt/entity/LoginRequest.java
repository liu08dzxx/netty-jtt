package com.vtradex.ehub.jtt.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 登陆请求对象
 * @author liuliwen
 *
 */
@Getter
@Setter
@ToString
public class LoginRequest {
	//用户名
    private int userId;
    //密码
    private String password;
    //从链路IP
    private String downLinkIp;
    //从链路端口
    private int downLinkPort;
 
}
