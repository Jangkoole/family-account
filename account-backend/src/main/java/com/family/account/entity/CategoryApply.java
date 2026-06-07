package com.family.account.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("category_apply")
public class CategoryApply {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long familyId;

    private Long userId;

    private String categoryName;

    private String type;

    private Integer status;

    private Long mergeToCategoryId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime applyTime;

    private LocalDateTime reviewTime;
}
