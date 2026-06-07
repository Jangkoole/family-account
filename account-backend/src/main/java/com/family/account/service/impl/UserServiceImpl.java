package com.family.account.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.family.account.common.Result;
import com.family.account.dto.user.*;
import com.family.account.entity.FamilyMember;
import com.family.account.entity.User;
import com.family.account.mapper.FamilyMemberMapper;
import com.family.account.mapper.UserMapper;
import com.family.account.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FamilyMemberMapper familyMemberMapper;

    // MD5加密密码
    private String encryptPassword(String password) {
        return DigestUtils.md5DigestAsHex(password.getBytes());
    }

    @Override
    public Result register(UserRegisterDTO dto) {
        // 检查账号是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getAccount, dto.getAccount());
        if (userMapper.selectOne(wrapper) != null) {
            return Result.error("账号已存在");
        }

        // 创建用户
        User user = new User();
        user.setAccount(dto.getAccount());
        user.setPassword(encryptPassword(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setStatus(1);
        userMapper.insert(user);

        return Result.success();
    }

    @Override
    public Result login(UserLoginDTO dto) {
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getAccount, dto.getAccount());
        User user = userMapper.selectOne(wrapper);

        // 账号或密码错误
        if (user == null || !user.getPassword().equals(encryptPassword(dto.getPassword()))) {
            return Result.error("账号或密码错误");
        }

        // 账号被禁用
        if (user.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }

        // 登录成功，生成token
        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("nickname", user.getNickname());

        return Result.success(data);
    }

    @Override
    public Result logout(Long userId) {
        StpUtil.logout(userId);
        return Result.success();
    }

    @Override
    public Result getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 查询家庭组信息
        LambdaQueryWrapper<FamilyMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FamilyMember::getUserId, userId);
        FamilyMember member = familyMemberMapper.selectOne(wrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("account", user.getAccount());
        data.put("nickname", user.getNickname());
        data.put("defaultVisible", user.getDefaultVisible() != null ? user.getDefaultVisible() : "PRIVATE");
        data.put("familyId", member != null ? member.getFamilyId() : null);
        data.put("familyRole", member != null ? member.getRole() : null);

        return Result.success(data);
    }

    @Override
    public Result updateNickname(Long userId, UserUpdateNicknameDTO dto) {
        User user = new User();
        user.setId(userId);
        user.setNickname(dto.getNickname());
        userMapper.updateById(user);
        return Result.success();
    }

    @Override
    public Result updatePassword(Long userId, UserUpdatePasswordDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 验证旧密码
        if (!user.getPassword().equals(encryptPassword(dto.getOldPassword()))) {
            return Result.error("旧密码错误");
        }

        // 更新密码
        user.setPassword(encryptPassword(dto.getNewPassword()));
        userMapper.updateById(user);
        return Result.success();
    }

    @Override
    public Result updateDefaultVisible(Long userId, UserUpdateDefaultVisibleDTO dto) {
        User user = new User();
        user.setId(userId);
        user.setDefaultVisible(dto.getDefaultVisible());
        userMapper.updateById(user);
        return Result.success();
    }
}