// FamilyMember.java
package com.family.account.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FamilyMember {
    private Long id;
    private Long familyId;
    private Long userId;
    private String role;   // ADMIN / MEMBER
    private LocalDateTime joinTime;
}