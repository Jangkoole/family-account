package com.family.account.dto.user;

import lombok.Data;

@Data
public class UserRegisterDTO {

    private String account;

    private String password;

    private String nickname;
}