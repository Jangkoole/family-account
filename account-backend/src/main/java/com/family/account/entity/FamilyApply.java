package com.family.account.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("family_apply")
public class FamilyApply {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long familyId;

    private Long userId;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime applyTime;

    private LocalDateTime reviewTime;
}