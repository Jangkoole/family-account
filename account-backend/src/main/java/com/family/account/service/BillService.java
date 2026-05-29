package com.family.account.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.family.account.entity.Bill;
import com.family.account.dto.bill.*;
import java.util.Map;

public interface BillService extends IService<Bill> {
    Long addBill(BillAddDTO dto, Long userId, Long familyId);
    void updateBill(BillUpdateDTO dto, Long userId, boolean isAdmin);
    void deleteBill(Long id, Long userId, boolean isAdmin);
    Map<String, Object> listBills(BillQueryDTO dto, Long userId, Long familyId, boolean isAdmin);
    Bill getBillDetail(Long id, Long userId, boolean isAdmin);
    void updateVisible(BillVisibleDTO dto, Long userId, boolean isAdmin);
    // 家庭管理员查询成员明细
    Map<String, Object> listFamilyBills(BillQueryDTO dto, Long adminId, Long familyId);
    // 导入相关（简化，重点展示框架）
    Map<String, Object> importBillsPreview(String filePath, String source, Long userId, Long familyId);
    Map<String, Object> importBills(String filePath, String source, Long userId, Long familyId);
}