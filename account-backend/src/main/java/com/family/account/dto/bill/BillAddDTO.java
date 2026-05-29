package com.family.account.dto.bill;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class BillAddDTO {

    @NotBlank(message = "收支类型不能为空")
    @Pattern(regexp = "INCOME|EXPENSE", message = "类型必须为INCOME或EXPENSE")
    private String type;

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount;

    @NotNull(message = "日期不能为空")
    private String date;  // yyyy-MM-dd

    private String note;

    // 可见范围，默认为PRIVATE，只有家庭成员可设置
    private String visible;
}