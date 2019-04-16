package com.vtradex.ehub.jtt.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LoginResponse {
    private int result;
    
    private int verifyCode;   
}

