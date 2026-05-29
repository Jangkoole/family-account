package com.family.account.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("bill")
public class Bill {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long familyId;

    private Long categoryId;

    private String type;        // INCOME / EXPENSE

    private BigDecimal amount;

    private LocalDate date;

    private String note;

    private String visible;     // PRIVATE / FAMILY

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 以下两个字段为数据库不存在，仅用于查询结果映射
    @TableField(exist = false)
    private String categoryName;

    @TableField(exist = false)
    private String nickname;
}
