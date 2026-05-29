package com.family.account.dto.bill;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BillAddDTO {
    private String type;          // INCOME / EXPENSE
    private Long categoryId;
    private BigDecimal amount;
    private LocalDate date;
    private String note;          // 可选
    private String visible;       // PRIVATE / FAMILY
}