package com.family.account.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CategoryAddDTO {

    @NotBlank(message = "分类名称不能为空")
    private String name;

    @NotBlank(message = "收支类型不能为空")
    @Pattern(regexp = "^(INCOME|EXPENSE)$", message = "收支类型必须为INCOME或EXPENSE")
    private String type;
}
