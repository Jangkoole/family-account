package com.family.account.dto.stat;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CategoryStatItemVO {
    private Long categoryId;
    private String categoryName;
    private BigDecimal amount;
    private BigDecimal percentage;
}
