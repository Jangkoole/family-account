package com.family.account.service;

import com.family.account.common.Result;
import com.family.account.dto.family.*;

public interface FamilyService {

    Result createFamily(Long userId, FamilyCreateDTO dto);

    Result getFamilyInfo(Long userId);

    Result joinFamily(Long userId, FamilyJoinDTO dto);

    Result getApplyList(Long userId);

    Result reviewApply(Long userId, FamilyReviewDTO dto);

    Result getMemberList(Long userId);

    Result removeMember(Long userId, Long targetUserId);

    Result transferAdmin(Long userId, FamilyTransferDTO dto);

    Result refreshInviteCode(Long userId);

    Result quitFamily(Long userId);

    Result dissolveFamily(Long userId);
}