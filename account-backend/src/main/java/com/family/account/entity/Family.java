package com.family.account.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("family")
public class Family {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Long adminId;

    private String inviteCode;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}