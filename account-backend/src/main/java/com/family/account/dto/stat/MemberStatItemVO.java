package com.family.account.dto.stat;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MemberStatItemVO {
    private Long userId;
    private String nickname;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;
    private BigDecimal incomePercentage;
    private BigDecimal expensePercentage;
}
