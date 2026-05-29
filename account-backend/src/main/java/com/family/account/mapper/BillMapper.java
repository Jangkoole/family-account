package com.family.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.family.account.entity.Bill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

@Mapper
public interface BillMapper extends BaseMapper<Bill> {

    /**
     * 个人账单分页查询（含可见性过滤）
     */
    Page<Bill> selectBillPage(Page<Bill> page,
                              @Param("userId") Long userId,
                              @Param("familyId") Long familyId,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate,
                              @Param("type") String type,
                              @Param("categoryId") Long categoryId,
                              @Param("visible") String visible,
                              @Param("minAmount") BigDecimal minAmount,
                              @Param("maxAmount") BigDecimal maxAmount);

    /**
     * 家庭管理员查询所有成员记录（无视可见性）
     */
    Page<Bill> selectFamilyBillPage(Page<Bill> page,
                                    @Param("familyId") Long familyId,
                                    @Param("userId") Long userId,
                                    @Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate,
                                    @Param("type") String type,
                                    @Param("categoryId") Long categoryId);

    /**
     * 查询记录详情（含分类名称）
     */
    Bill selectDetailById(@Param("id") Long id);

    /**
     * 根据用户ID获取其所属家庭组ID（从 family_member 表）
     */
    Long selectFamilyIdByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID获取其在家庭组中的角色（从 family_member 表）
     */
    String selectFamilyRoleByUserId(@Param("userId") Long userId);
}