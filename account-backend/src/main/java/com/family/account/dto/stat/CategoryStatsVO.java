package com.family.account.dto.stat;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CategoryStatsVO {
    private BigDecimal totalAmount;
    private List<CategoryStatItemVO> list;
}
