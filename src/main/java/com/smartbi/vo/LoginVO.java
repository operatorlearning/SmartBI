package com.smartbi.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录响应
 */
@Data
public class LoginVO implements Serializable {

    private String token;
    private UserVO user;
}
