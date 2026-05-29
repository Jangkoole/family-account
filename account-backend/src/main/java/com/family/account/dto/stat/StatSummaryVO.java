package com.family.account.dto.stat;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class StatSummaryVO {
    private String startDate;
    private String endDate;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;
}
