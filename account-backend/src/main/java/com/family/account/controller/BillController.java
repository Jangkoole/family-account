package com.family.account.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.family.account.common.Result;
import com.family.account.dto.bill.*;
import com.family.account.entity.Bill;
import com.family.account.entity.FamilyMember;
import com.family.account.mapper.FamilyMemberMapper;
import com.family.account.service.BillService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/bill")
@RequiredArgsConstructor
@SaCheckLogin
public class BillController {

    private final BillService billService;
    private final FamilyMemberMapper familyMemberMapper;

    // 获取当前用户ID和家庭信息
    private Long getCurrentUserId() {
        return StpUtil.getLoginIdAsLong();
    }

    private Long getCurrentFamilyId() {
        Long userId = getCurrentUserId();
        FamilyMember member = familyMemberMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FamilyMember>()
                        .eq(FamilyMember::getUserId, userId));
        return member != null ? member.getFamilyId() : null;
    }

    private boolean isFamilyAdmin(Long userId, Long familyId) {
        if (familyId == null) return false;
        FamilyMember member = familyMemberMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FamilyMember>()
                        .eq(FamilyMember::getUserId, userId)
                        .eq(FamilyMember::getFamilyId, familyId));
        return member != null && "ADMIN".equals(member.getRole());
    }

    // 3.1 新增收支记录
    @PostMapping("/add")
    public Result<Long> addBill(@RequestBody BillAddDTO dto) {
        Long userId = getCurrentUserId();
        Long familyId = getCurrentFamilyId();
        Long id = billService.addBill(dto, userId, familyId);
        return Result.success(id);
    }

    // 3.2 修改收支记录
    @PutMapping("/update")
    public Result<Void> updateBill(@RequestBody BillUpdateDTO dto) {
        Long userId = getCurrentUserId();
        Long familyId = getCurrentFamilyId();
        boolean isAdmin = isFamilyAdmin(userId, familyId);
        billService.updateBill(dto, userId, isAdmin);
        return Result.success();
    }

    // 3.3 删除收支记录
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteBill(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        Long familyId = getCurrentFamilyId();
        boolean isAdmin = isFamilyAdmin(userId, familyId);
        billService.deleteBill(id, userId, isAdmin);
        return Result.success();
    }

    // 3.4 查询收支记录列表
    @GetMapping("/list")
    public Result<Object> listBills(BillQueryDTO dto) {
        Long userId = getCurrentUserId();
        Long familyId = getCurrentFamilyId();
        boolean isAdmin = isFamilyAdmin(userId, familyId);
        return Result.success(billService.listBills(dto, userId, familyId, isAdmin));
    }

    // 3.5 获取收支记录详情
    @GetMapping("/detail/{id}")
    public Result<Bill> detail(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        Long familyId = getCurrentFamilyId();
        boolean isAdmin = isFamilyAdmin(userId, familyId);
        return Result.success(billService.getBillDetail(id, userId, isAdmin));
    }

    // 3.6 修改记录可见范围
    @PutMapping("/visible")
    public Result<Void> updateVisible(@RequestBody BillVisibleDTO dto) {
        Long userId = getCurrentUserId();
        Long familyId = getCurrentFamilyId();
        boolean isAdmin = isFamilyAdmin(userId, familyId);
        billService.updateVisible(dto, userId, isAdmin);
        return Result.success();
    }

    // 3.9 家庭管理员查询成员记录明细
    @GetMapping("/family/list")
    public Result<Object> listFamilyBills(BillQueryDTO dto) {
        Long adminId = getCurrentUserId();
        Long familyId = getCurrentFamilyId();
        if (familyId == null || !isFamilyAdmin(adminId, familyId))
            return Result.error(403, "无权限，仅家庭管理员可查看");
        return Result.success(billService.listFamilyBills(dto, adminId, familyId));
    }

    // 3.7 批量导入（正式）
    @PostMapping("/import")
    public Result<Object> importBills(@RequestParam("file") MultipartFile file,
                                      @RequestParam("source") String source) {
        // 保存临时文件，调用 service
        // 此处省略文件保存代码，实际可将 MultipartFile 转为 InputStream 解析
        Long userId = getCurrentUserId();
        Long familyId = getCurrentFamilyId();
        // 调用 service.importBills(...)
        return Result.success(billService.importBills(null, source, userId, familyId));
    }

    // 3.8 预览导入映射结果
    @PostMapping("/import/preview")
    public Result<Object> previewImport(@RequestParam("file") MultipartFile file,
                                        @RequestParam("source") String source) {
        Long userId = getCurrentUserId();
        Long familyId = getCurrentFamilyId();
        return Result.success(billService.importBillsPreview(null, source, userId, familyId));
    }
}
