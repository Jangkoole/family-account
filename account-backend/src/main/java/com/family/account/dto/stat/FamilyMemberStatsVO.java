package com.family.account.dto.stat;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class FamilyMemberStatsVO {
    private BigDecimal familyTotalIncome;
    private BigDecimal familyTotalExpense;
    private BigDecimal familyBalance;
    private List<MemberStatItemVO> members;
}
