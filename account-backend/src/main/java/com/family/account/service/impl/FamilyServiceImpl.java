package com.family.account.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.family.account.common.Result;
import com.family.account.dto.family.*;
import com.family.account.entity.Family;
import com.family.account.entity.FamilyApply;
import com.family.account.entity.FamilyMember;
import com.family.account.entity.User;
import com.family.account.mapper.FamilyApplyMapper;
import com.family.account.mapper.FamilyMapper;
import com.family.account.mapper.FamilyMemberMapper;
import com.family.account.mapper.UserMapper;
import com.family.account.service.FamilyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FamilyServiceImpl implements FamilyService {

    @Autowired
    private FamilyMapper familyMapper;

    @Autowired
    private FamilyMemberMapper familyMemberMapper;

    @Autowired
    private FamilyApplyMapper familyApplyMapper;

    @Autowired
    private UserMapper userMapper;

    // 生成邀请码
    private String generateInviteCode() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    // 获取用户所在家庭组成员记录
    private FamilyMember getFamilyMember(Long userId) {
        LambdaQueryWrapper<FamilyMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FamilyMember::getUserId, userId);
        return familyMemberMapper.selectOne(wrapper);
    }

    @Override
    public Result createFamily(Long userId, FamilyCreateDTO dto) {
        // 检查是否已在家庭组
        if (getFamilyMember(userId) != null) {
            return Result.error("您已加入家庭组，请先退出");
        }

        // 创建家庭组
        Family family = new Family();
        family.setName(dto.getName());
        family.setAdminId(userId);
        family.setInviteCode(generateInviteCode());
        familyMapper.insert(family);

        // 创建者自动成为管理员成员
        FamilyMember member = new FamilyMember();
        member.setFamilyId(family.getId());
        member.setUserId(userId);
        member.setRole("ADMIN");
        familyMemberMapper.insert(member);

        Map<String, Object> data = new HashMap<>();
        data.put("familyId", family.getId());
        data.put("name", family.getName());
        data.put("inviteCode", family.getInviteCode());
        return Result.success(data);
    }

    @Override
    public Result getFamilyInfo(Long userId) {
        FamilyMember member = getFamilyMember(userId);
        if (member == null) {
            return Result.error("您还未加入任何家庭组");
        }

        Family family = familyMapper.selectById(member.getFamilyId());
        User admin = userMapper.selectById(family.getAdminId());

        // 查询成员数量
        LambdaQueryWrapper<FamilyMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FamilyMember::getFamilyId, family.getId());
        Long memberCount = familyMemberMapper.selectCount(wrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("familyId", family.getId());
        data.put("name", family.getName());
        data.put("inviteCode", family.getInviteCode());
        data.put("adminId", family.getAdminId());
        data.put("adminNickname", admin.getNickname());
        data.put("memberCount", memberCount);
        return Result.success(data);
    }

    @Override
    public Result joinFamily(Long userId, FamilyJoinDTO dto) {
        // 检查是否已在家庭组
        if (getFamilyMember(userId) != null) {
            return Result.error("您已加入家庭组，请先退出");
        }

        // 去除空格
        String inviteCode = dto.getInviteCode().trim();

        // 查找家庭组
        LambdaQueryWrapper<Family> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Family::getInviteCode, inviteCode);
        Family family = familyMapper.selectOne(wrapper);
        if (family == null) {
            return Result.error("邀请码无效");
        }

        // 检查是否已有待审核申请
        LambdaQueryWrapper<FamilyApply> applyWrapper = new LambdaQueryWrapper<>();
        applyWrapper.eq(FamilyApply::getUserId, userId)
                .eq(FamilyApply::getFamilyId, family.getId())
                .eq(FamilyApply::getStatus, 0);
        if (familyApplyMapper.selectOne(applyWrapper) != null) {
            return Result.error("您已提交申请，请等待审核");
        }

        // 创建申请
        FamilyApply apply = new FamilyApply();
        apply.setFamilyId(family.getId());
        apply.setUserId(userId);
        apply.setStatus(0);
        familyApplyMapper.insert(apply);

        return Result.success();
    }

    @Override
    public Result getApplyList(Long userId) {
        FamilyMember member = getFamilyMember(userId);
        if (member == null || !member.getRole().equals("ADMIN")) {
            return Result.error(403, "无权限");
        }

        LambdaQueryWrapper<FamilyApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FamilyApply::getFamilyId, member.getFamilyId())
                .eq(FamilyApply::getStatus, 0);
        List<FamilyApply> applies = familyApplyMapper.selectList(wrapper);

        List<Map<String, Object>> list = new ArrayList<>();
        for (FamilyApply apply : applies) {
            User user = userMapper.selectById(apply.getUserId());
            Map<String, Object> item = new HashMap<>();
            item.put("applyId", apply.getId());
            item.put("userId", apply.getUserId());
            item.put("nickname", user.getNickname());
            item.put("account", user.getAccount());
            item.put("applyTime", apply.getApplyTime());
            list.add(item);
        }

        return Result.success(list);
    }

    @Override
    public Result reviewApply(Long userId, FamilyReviewDTO dto) {
        FamilyMember member = getFamilyMember(userId);
        if (member == null || !member.getRole().equals("ADMIN")) {
            return Result.error(403, "无权限");
        }

        FamilyApply apply = familyApplyMapper.selectById(dto.getApplyId());
        if (apply == null || apply.getStatus() != 0) {
            return Result.error("申请不存在或已处理");
        }

        apply.setStatus(dto.getApprove() ? 1 : 2);
        apply.setReviewTime(LocalDateTime.now());
        familyApplyMapper.updateById(apply);

        if (dto.getApprove()) {
            FamilyMember newMember = new FamilyMember();
            newMember.setFamilyId(apply.getFamilyId());
            newMember.setUserId(apply.getUserId());
            newMember.setRole("MEMBER");
            familyMemberMapper.insert(newMember);
        }

        return Result.success();
    }

    @Override
    public Result getMemberList(Long userId) {
        FamilyMember member = getFamilyMember(userId);
        if (member == null) {
            return Result.error("您还未加入任何家庭组");
        }

        LambdaQueryWrapper<FamilyMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FamilyMember::getFamilyId, member.getFamilyId());
        List<FamilyMember> members = familyMemberMapper.selectList(wrapper);

        List<Map<String, Object>> list = new ArrayList<>();
        for (FamilyMember m : members) {
            User user = userMapper.selectById(m.getUserId());
            Map<String, Object> item = new HashMap<>();
            item.put("userId", m.getUserId());
            item.put("nickname", user.getNickname());
            item.put("account", user.getAccount());
            item.put("role", m.getRole());
            item.put("joinTime", m.getJoinTime());
            list.add(item);
        }

        return Result.success(list);
    }

    // 移除成员时应该同时把该成员的待审核申请也清理掉
    @Override
    public Result removeMember(Long userId, Long targetUserId) {
        FamilyMember member = getFamilyMember(userId);
        if (member == null || !member.getRole().equals("ADMIN")) {
            return Result.error(403, "无权限");
        }

        if (userId.equals(targetUserId)) {
            return Result.error("不能移除自己，请转让管理员后再退出");
        }

        // 移除成员
        LambdaQueryWrapper<FamilyMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FamilyMember::getFamilyId, member.getFamilyId())
                .eq(FamilyMember::getUserId, targetUserId);
        familyMemberMapper.delete(wrapper);

        // 清理该成员的待审核申请
        LambdaQueryWrapper<FamilyApply> applyWrapper = new LambdaQueryWrapper<>();
        applyWrapper.eq(FamilyApply::getFamilyId, member.getFamilyId())
                .eq(FamilyApply::getUserId, targetUserId)
                .eq(FamilyApply::getStatus, 0);
        familyApplyMapper.delete(applyWrapper);

        return Result.success();
    }

    @Override
    public Result transferAdmin(Long userId, FamilyTransferDTO dto) {
        FamilyMember member = getFamilyMember(userId);
        if (member == null || !member.getRole().equals("ADMIN")) {
            return Result.error(403, "无权限");
        }

        // 更新原管理员为普通成员
        member.setRole("MEMBER");
        familyMemberMapper.updateById(member);

        // 更新新管理员
        LambdaQueryWrapper<FamilyMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FamilyMember::getFamilyId, member.getFamilyId())
                .eq(FamilyMember::getUserId, dto.getUserId());
        FamilyMember newAdmin = familyMemberMapper.selectOne(wrapper);
        if (newAdmin == null) {
            return Result.error("该用户不在家庭组内");
        }
        newAdmin.setRole("ADMIN");
        familyMemberMapper.updateById(newAdmin);

        // 更新family表的adminId
        Family family = familyMapper.selectById(member.getFamilyId());
        family.setAdminId(dto.getUserId());
        familyMapper.updateById(family);

        return Result.success();
    }

    @Override
    public Result refreshInviteCode(Long userId) {
        FamilyMember member = getFamilyMember(userId);
        if (member == null || !member.getRole().equals("ADMIN")) {
            return Result.error(403, "无权限");
        }

        Family family = familyMapper.selectById(member.getFamilyId());
        family.setInviteCode(generateInviteCode());
        familyMapper.updateById(family);

        Map<String, Object> data = new HashMap<>();
        data.put("inviteCode", family.getInviteCode());
        return Result.success(data);
    }

    @Override
    public Result quitFamily(Long userId) {
        FamilyMember member = getFamilyMember(userId);
        if (member == null) {
            return Result.error("您还未加入任何家庭组");
        }

        if (member.getRole().equals("ADMIN")) {
            return Result.error("管理员不能直接退出，请先转让管理员身份");
        }

        familyMemberMapper.deleteById(member.getId());
        return Result.success();
    }

    @Override
    public Result dissolveFamily(Long userId) {
        FamilyMember member = getFamilyMember(userId);
        if (member == null || !member.getRole().equals("ADMIN")) {
            return Result.error(403, "无权限");
        }

        Long familyId = member.getFamilyId();

        // 删除所有成员
        LambdaQueryWrapper<FamilyMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(FamilyMember::getFamilyId, familyId);
        familyMemberMapper.delete(memberWrapper);

        // 删除所有申请记录
        LambdaQueryWrapper<FamilyApply> applyWrapper = new LambdaQueryWrapper<>();
        applyWrapper.eq(FamilyApply::getFamilyId, familyId);
        familyApplyMapper.delete(applyWrapper);

        // 删除家庭组
        familyMapper.deleteById(familyId);

        return Result.success();
    }
}