package com.family.account.service;

import com.family.account.dto.user.*;
import com.family.account.common.Result;

public interface UserService {

    Result register(UserRegisterDTO dto);

    Result login(UserLoginDTO dto);

    Result logout(Long userId);

    Result getUserInfo(Long userId);

    Result updateNickname(Long userId, UserUpdateNicknameDTO dto);

    Result updatePassword(Long userId, UserUpdatePasswordDTO dto);

    Result updateDefaultVisible(Long userId, UserUpdateDefaultVisibleDTO dto);
}
