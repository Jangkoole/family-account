package com.family.account.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("family_member")
public class FamilyMember {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long familyId;

    private Long userId;

    private String role;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime joinTime;
}