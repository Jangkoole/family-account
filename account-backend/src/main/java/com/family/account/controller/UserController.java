package com.family.account.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.family.account.common.Result;
import com.family.account.dto.user.*;
import com.family.account.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // 注册
    @PostMapping("/register")
    public Result register(@RequestBody UserRegisterDTO dto) {
        return userService.register(dto);
    }

    // 登录
    @PostMapping("/login")
    public Result login(@RequestBody UserLoginDTO dto) {
        return userService.login(dto);
    }

    // 登出
    @PostMapping("/logout")
    public Result logout() {
        Long userId = StpUtil.getLoginIdAsLong();
        return userService.logout(userId);
    }

    // 获取个人信息
    @GetMapping("/info")
    public Result getUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        return userService.getUserInfo(userId);
    }

    // 修改昵称
    @PutMapping("/nickname")
    public Result updateNickname(@RequestBody UserUpdateNicknameDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        return userService.updateNickname(userId, dto);
    }

    // 修改密码
    @PutMapping("/password")
    public Result updatePassword(@RequestBody UserUpdatePasswordDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        return userService.updatePassword(userId, dto);
    }
}