package com.smartbi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求
 */
@Data
public class UserRegisterDTO implements Serializable {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度3-20")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度6-32")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    private String email;
}
