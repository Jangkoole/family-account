// Category.java
package com.family.account.entity;

import lombok.Data;

@Data
public class Category {
    private Long id;
    private String name;
    private String type;   // INCOME / EXPENSE
    private Boolean isSystem;
    private Long userId;
    private Long familyId;
}