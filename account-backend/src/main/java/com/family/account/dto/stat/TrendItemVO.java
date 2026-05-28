package com.family.account.dto.stat;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TrendItemVO {
    private String date;
    private BigDecimal income;
    private BigDecimal expense;
}
