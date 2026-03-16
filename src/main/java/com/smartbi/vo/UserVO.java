package com.smartbi.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息响应
 */
@Data
public class UserVO implements Serializable {

    private Long id;
    private String username;
    private String email;
    private String avatar;
    private String role;
    private LocalDateTime createTime;
}
