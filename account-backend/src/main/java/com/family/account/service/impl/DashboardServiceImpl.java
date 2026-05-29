package com.family.account.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.family.account.common.Result;
import com.family.account.entity.FamilyMember;
import com.family.account.mapper.FamilyMemberMapper;
import com.family.account.mapper.StatMapper;
import com.family.account.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private StatMapper statMapper;

    @Autowired
    private FamilyMemberMapper familyMemberMapper;

    @Override
    public Result getDashboardSummary(Long userId) {
        LocalDate today = LocalDate.now();

        // 1. 本月汇总
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.with(TemporalAdjusters.lastDayOfMonth());
        Map<String, Object> monthSummary = statMapper.selectSummaryByUserAndDate(userId, monthStart, monthEnd);

        // 2. 本月各分类支出占比（饼图）
        List<Map<String, Object>> categoryRaw = statMapper.selectCategoryStat(userId, "EXPENSE", monthStart, monthEnd);
        BigDecimal totalExpense = (BigDecimal) monthSummary.get("totalExpense");
        List<Map<String, Object>> categoryChart = new ArrayList<>();
        for (Map<String, Object> row : categoryRaw) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("categoryName", row.get("categoryName"));
            item.put("amount", row.get("amount"));
            BigDecimal amount = (BigDecimal) row.get("amount");
            double percentage = totalExpense.compareTo(BigDecimal.ZERO) > 0
                    ? amount.multiply(BigDecimal.valueOf(100)).divide(totalExpense, 2, RoundingMode.HALF_UP).doubleValue()
                    : 0.0;
            item.put("percentage", percentage);
            categoryChart.add(item);
        }

        // 3. 最近7天收支趋势（折线图）
        LocalDate weekStart = today.minusDays(6);
        List<Map<String, Object>> trendRaw = statMapper.selectDailyTrend(userId, weekStart, today);
        // 填充缺失日期
        List<Map<String, Object>> trendChart = new ArrayList<>();
        Map<LocalDate, Map<String, Object>> trendMap = new HashMap<>();
        for (Map<String, Object> row : trendRaw) {
            trendMap.put(((java.sql.Date) row.get("date")).toLocalDate(), row);
        }
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", date.toString());
            if (trendMap.containsKey(date)) {
                item.put("income", trendMap.get(date).get("income"));
                item.put("expense", trendMap.get(date).get("expense"));
            } else {
                item.put("income", BigDecimal.ZERO);
                item.put("expense", BigDecimal.ZERO);
            }
            trendChart.add(item);
        }

        // 4. 最近10条收支记录
        List<Map<String, Object>> recentBills = statMapper.selectRecentBills(userId, 10);

        // 5. 家庭概况
        Map<String, Object> familySummary = null;
        LambdaQueryWrapper<FamilyMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(FamilyMember::getUserId, userId);
        FamilyMember member = familyMemberMapper.selectOne(memberWrapper);
        if (member != null) {
            Map<String, Object> familyData = statMapper.selectFamilySummary(member.getFamilyId(), userId, monthStart, monthEnd);
            familySummary = new LinkedHashMap<>();
            familySummary.put("familyTotalIncome", familyData.get("totalIncome"));
            familySummary.put("familyTotalExpense", familyData.get("totalExpense"));
            BigDecimal fi = (BigDecimal) familyData.get("totalIncome");
            BigDecimal fe = (BigDecimal) familyData.get("totalExpense");
            familySummary.put("familyBalance", fi.subtract(fe));
        }

        // 组装返回数据
        Map<String, Object> data = new LinkedHashMap<>();
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalIncome", monthSummary.get("totalIncome"));
        summary.put("totalExpense", monthSummary.get("totalExpense"));
        BigDecimal ti = (BigDecimal) monthSummary.get("totalIncome");
        BigDecimal te = (BigDecimal) monthSummary.get("totalExpense");
        summary.put("balance", ti.subtract(te));
        data.put("monthSummary", summary);
        data.put("categoryChart", categoryChart);
        data.put("trendChart", trendChart);
        data.put("recentBills", recentBills);
        data.put("familySummary", familySummary);

        return Result.success(data);
    }
}
