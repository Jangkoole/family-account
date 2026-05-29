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

    private Long userId;          // 记录人用户ID

    private Long familyId;        // 所属家庭组ID，独立用户为null

    private Long categoryId;      // 分类ID

    private String type;          // INCOME / EXPENSE

    private BigDecimal amount;    // 金额

    private LocalDate date;       // 记账日期

    private String note;          // 备注

    private String visible;       // PRIVATE / FAMILY

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
