package com.family.account.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.family.account.common.Result;
import com.family.account.service.StatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stat")
public class StatController {

    @Autowired
    private StatService statService;

    @GetMapping("/summary")
    public Result summary(@RequestParam String type, @RequestParam String date) {
        Long userId = StpUtil.getLoginIdAsLong();
        return statService.summary(userId, type, date);
    }

    @GetMapping("/category")
    public Result categoryStats(@RequestParam String type,
                                @RequestParam String startDate,
                                @RequestParam String endDate) {
        Long userId = StpUtil.getLoginIdAsLong();
        return statService.categoryStats(userId, type, startDate, endDate);
    }

    @GetMapping("/trend")
    public Result trend(@RequestParam String granularity,
                        @RequestParam String startDate,
                        @RequestParam String endDate) {
        Long userId = StpUtil.getLoginIdAsLong();
        return statService.trend(userId, granularity, startDate, endDate);
    }

    @GetMapping("/family/members")
    public Result familyMemberStats(@RequestParam String startDate,
                                    @RequestParam String endDate) {
        Long userId = StpUtil.getLoginIdAsLong();
        return statService.familyMemberStats(userId, startDate, endDate);
    }
}
