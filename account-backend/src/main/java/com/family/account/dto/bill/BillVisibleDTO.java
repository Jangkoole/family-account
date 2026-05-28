package com.family.account.dto.bill;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class BillVisibleDTO {

    @NotEmpty(message = "记录ID列表不能为空")
    private List<Long> ids;

    @NotBlank(message = "可见范围不能为空")
    @Pattern(regexp = "PRIVATE|FAMILY", message = "可见范围必须为PRIVATE或FAMILY")
    private String visible;
}