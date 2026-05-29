package com.family.account.dto.bill;

import jakarta.validation.constraints.Min;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class BillQueryDTO {

    private String startDate;   // yyyy-MM-dd
    private String endDate;
    private String type;        // INCOME / EXPENSE
    private Long categoryId;
    private String visible;     // PRIVATE / FAMILY
    private BigDecimal minAmount;
    private BigDecimal maxAmount;

    @Min(value = 1, message = "页码从1开始")
    private Integer page = 1;

    @Min(value = 1, message = "每页条数至少为1")
    private Integer pageSize = 20;
}