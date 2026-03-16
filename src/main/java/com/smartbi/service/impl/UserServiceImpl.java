package com.smartbi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartbi.common.BusinessException;
import com.smartbi.common.ErrorCode;
import com.smartbi.dto.UserLoginDTO;
import com.smartbi.dto.UserRegisterDTO;
import com.smartbi.entity.User;
import com.smartbi.mapper.UserMapper;
import com.smartbi.security.JwtTokenProvider;
import com.smartbi.service.UserService;
import com.smartbi.vo.LoginVO;
import com.smartbi.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public UserVO register(UserRegisterDTO dto) {
        // 1. 校验两次密码一致
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "两次输入的密码不一致");
        }

        // 2. 校验用户名唯一
        long count = count(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.USER_EXISTS);
        }

        // 3. 创建用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setRole("user");
        user.setStatus(1);
        save(user);

        // 4. 返回用户信息
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    public LoginVO login(UserLoginDTO dto) {
        // 1. Spring Security 认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));

        // 2. 生成 JWT Token
        String token = jwtTokenProvider.generateToken(authentication);

        // 3. 查询用户信息
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        // 4. 组装响应
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUser(userVO);
        return loginVO;
    }

    @Override
    public UserVO getCurrentUser() {
        Long userId = getCurrentUserId();
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        String username = auth.getName();
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user.getId();
    }
}
