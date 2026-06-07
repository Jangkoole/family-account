package com.family.account.dto.category;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategoryMigrateDTO {

    @NotNull(message = "源分类ID不能为空")
    private Long fromCategoryId;

    @NotNull(message = "目标分类ID不能为空")
    private Long toCategoryId;
}
