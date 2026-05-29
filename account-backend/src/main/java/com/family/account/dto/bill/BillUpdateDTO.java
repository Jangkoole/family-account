package com.family.account.dto.bill;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BillUpdateDTO {
    private Long id;
    private String type;
    private Long categoryId;
    private BigDecimal amount;
    private LocalDate date;
    private String note;
    private String visible;
}