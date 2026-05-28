package com.family.account.dto.user;

import lombok.Data;

@Data
public class UserUpdatePasswordDTO {

    private String oldPassword;

    private String newPassword;
}