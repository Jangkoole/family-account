package com.family.account.service;

import com.family.account.common.Result;

public interface StatService {

    Result summary(Long userId, String type, String date);

    Result categoryStats(Long userId, String type, String startDate, String endDate);

    Result trend(Long userId, String granularity, String startDate, String endDate);

    Result familyMemberStats(Long userId, String startDate, String endDate);
}
