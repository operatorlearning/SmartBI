package com.smartbi.controller;

import com.smartbi.annotation.OperLog;
import com.smartbi.common.Result;
import com.smartbi.dto.UserLoginDTO;
import com.smartbi.dto.UserRegisterDTO;
import com.smartbi.service.UserService;
import com.smartbi.vo.LoginVO;
import com.smartbi.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户接口
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    @OperLog("用户注册")
    public Result<UserVO> register(@RequestBody @Valid UserRegisterDTO dto) {
        return Result.success(userService.register(dto));
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    @OperLog("用户登录")
    public Result<LoginVO> login(@RequestBody @Valid UserLoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    @GetMapping("/current")
    @Operation(summary = "获取当前用户信息")
    public Result<UserVO> getCurrentUser() {
        return Result.success(userService.getCurrentUser());
    }
}
