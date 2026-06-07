package com.family.account.dto.category;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategoryReviewDTO {

    @NotNull(message = "申请ID不能为空")
    private Long applyId;

    @NotNull(message = "审核结果不能为空")
    private Boolean approve;

    private Long mergeToCategoryId;
}
