package com.family.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.account.entity.Bill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface BillMapper extends BaseMapper<Bill> {

    List<Map<String, Object>> selectBillListWithCategory(
            @Param("userId") Long userId,
            @Param("familyId") Long familyId,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("type") String type,
            @Param("categoryId") Long categoryId,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("visibleForMember") String visibleForMember,
            @Param("isAdmin") boolean isAdmin,
            @Param("targetUserId") Long targetUserId
    );

    Long countBillList(
            @Param("userId") Long userId,
            @Param("familyId") Long familyId,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("type") String type,
            @Param("categoryId") Long categoryId,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("visibleForMember") String visibleForMember,
            @Param("isAdmin") boolean isAdmin,
            @Param("targetUserId") Long targetUserId
    );
}
