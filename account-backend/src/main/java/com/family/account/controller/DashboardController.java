package com.family.account.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.family.account.common.Result;
import com.family.account.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/summary")
    public Result getDashboardSummary() {
        Long userId = StpUtil.getLoginIdAsLong();
        return dashboardService.getDashboardSummary(userId);
    }
}
