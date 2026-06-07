package com.family.account.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserUpdateDefaultVisibleDTO {

    @NotBlank(message = "可见范围不能为空")
    @Pattern(regexp = "PRIVATE|FAMILY", message = "可见范围必须为PRIVATE或FAMILY")
    private String defaultVisible;
}
