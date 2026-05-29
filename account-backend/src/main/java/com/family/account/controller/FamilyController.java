package com.family.account.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.family.account.common.Result;
import com.family.account.dto.family.*;
import com.family.account.service.FamilyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/family")
public class FamilyController {

    @Autowired
    private FamilyService familyService;

    // 创建家庭组
    @PostMapping("/create")
    public Result createFamily(@RequestBody FamilyCreateDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        return familyService.createFamily(userId, dto);
    }

    // 获取家庭组信息
    @GetMapping("/info")
    public Result getFamilyInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        return familyService.getFamilyInfo(userId);
    }

    // 申请加入家庭组
    @PostMapping("/join")
    public Result joinFamily(@RequestBody FamilyJoinDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        return familyService.joinFamily(userId, dto);
    }

    // 获取加入申请列表
    @GetMapping("/apply/list")
    public Result getApplyList() {
        Long userId = StpUtil.getLoginIdAsLong();
        return familyService.getApplyList(userId);
    }

    // 审核加入申请
    @PutMapping("/apply/review")
    public Result reviewApply(@RequestBody FamilyReviewDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        return familyService.reviewApply(userId, dto);
    }

    // 获取家庭成员列表
    @GetMapping("/member/list")
    public Result getMemberList() {
        Long userId = StpUtil.getLoginIdAsLong();
        return familyService.getMemberList(userId);
    }

    // 移除家庭成员
    @DeleteMapping("/member/remove/{userId}")
    public Result removeMember(@PathVariable Long userId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        return familyService.removeMember(currentUserId, userId);
    }

    // 转让管理员
    @PutMapping("/admin/transfer")
    public Result transferAdmin(@RequestBody FamilyTransferDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        return familyService.transferAdmin(userId, dto);
    }

    // 重新生成邀请码
    @PutMapping("/invite/refresh")
    public Result refreshInviteCode() {
        Long userId = StpUtil.getLoginIdAsLong();
        return familyService.refreshInviteCode(userId);
    }

    // 退出家庭组
    @PostMapping("/quit")
    public Result quitFamily() {
        Long userId = StpUtil.getLoginIdAsLong();
        return familyService.quitFamily(userId);
    }

    // 解散家庭组
    @DeleteMapping("/dissolve")
    public Result dissolveFamily() {
        Long userId = StpUtil.getLoginIdAsLong();
        return familyService.dissolveFamily(userId);
    }
}
