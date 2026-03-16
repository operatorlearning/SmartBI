package com.smartbi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartbi.dto.UserLoginDTO;
import com.smartbi.dto.UserRegisterDTO;
import com.smartbi.entity.User;
import com.smartbi.vo.LoginVO;
import com.smartbi.vo.UserVO;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     */
    UserVO register(UserRegisterDTO dto);

    /**
     * 用户登录
     */
    LoginVO login(UserLoginDTO dto);

    /**
     * 获取当前登录用户信息
     */
    UserVO getCurrentUser();

    /**
     * 获取当前登录用户ID
     */
    Long getCurrentUserId();
}
