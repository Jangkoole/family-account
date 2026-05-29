package com.family.account.dto.bill;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BillQueryDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private String type;          // INCOME / EXPENSE
    private Long categoryId;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Integer page = 1;
    private Integer pageSize = 20;
}
