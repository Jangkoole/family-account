package com.family.account.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.family.account.common.Result;
import com.family.account.dto.stat.*;
import com.family.account.entity.FamilyMember;
import com.family.account.mapper.FamilyMemberMapper;
import com.family.account.mapper.StatMapper;
import com.family.account.service.StatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class StatServiceImpl implements StatService {

    @Autowired
    private StatMapper statMapper;

    @Autowired
    private FamilyMemberMapper familyMemberMapper;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public Result summary(Long userId, String type, String dateStr) {
        LocalDate date = LocalDate.parse(dateStr, DATE_FMT);
        LocalDate startDate;
        LocalDate endDate;

        switch (type.toUpperCase()) {
            case "DAY":
                startDate = date;
                endDate = date;
                break;
            case "WEEK":
                startDate = date.with(DayOfWeek.MONDAY);
                endDate = startDate.plusDays(6);
                break;
            case "MONTH":
                startDate = date.withDayOfMonth(1);
                endDate = date.withDayOfMonth(date.lengthOfMonth());
                break;
            case "YEAR":
                startDate = date.withDayOfYear(1);
                endDate = date.withDayOfYear(date.lengthOfYear());
                break;
            default:
                return Result.error("type参数无效，可选值：DAY/WEEK/MONTH/YEAR");
        }

        Map<String, BigDecimal> row = statMapper.summary(userId, startDate, endDate);
        BigDecimal totalIncome = row.getOrDefault("totalIncome", BigDecimal.ZERO);
        BigDecimal totalExpense = row.getOrDefault("totalExpense", BigDecimal.ZERO);

        StatSummaryVO vo = new StatSummaryVO();
        vo.setStartDate(startDate.format(DATE_FMT));
        vo.setEndDate(endDate.format(DATE_FMT));
        vo.setTotalIncome(totalIncome);
        vo.setTotalExpense(totalExpense);
        vo.setBalance(totalIncome.subtract(totalExpense));

        return Result.success(vo);
    }

    @Override
    public Result categoryStats(Long userId, String type, String startDateStr, String endDateStr) {
        if (!"INCOME".equalsIgnoreCase(type) && !"EXPENSE".equalsIgnoreCase(type)) {
            return Result.error("type参数无效，可选值：INCOME/EXPENSE");
        }

        LocalDate startDate = LocalDate.parse(startDateStr, DATE_FMT);
        LocalDate endDate = LocalDate.parse(endDateStr, DATE_FMT);

        List<Map<String, Object>> rows = statMapper.categoryStats(userId, type.toUpperCase(), startDate, endDate);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Map<String, Object> row : rows) {
            totalAmount = totalAmount.add((BigDecimal) row.get("amount"));
        }

        List<CategoryStatItemVO> list = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            CategoryStatItemVO item = new CategoryStatItemVO();
            item.setCategoryId((Long) row.get("categoryId"));
            item.setCategoryName((String) row.get("categoryName"));
            BigDecimal amount = (BigDecimal) row.get("amount");
            item.setAmount(amount);
            item.setPercentage(totalAmount.compareTo(BigDecimal.ZERO) > 0
                    ? amount.multiply(new BigDecimal("100")).divide(totalAmount, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);
            list.add(item);
        }

        CategoryStatsVO vo = new CategoryStatsVO();
        vo.setTotalAmount(totalAmount);
        vo.setList(list);

        return Result.success(vo);
    }

    @Override
    public Result trend(Long userId, String granularity, String startDateStr, String endDateStr) {
        String gran = granularity.toUpperCase();
        if (!"DAY".equals(gran) && !"WEEK".equals(gran) && !"MONTH".equals(gran)) {
            return Result.error("granularity参数无效，可选值：DAY/WEEK/MONTH");
        }

        LocalDate startDate = LocalDate.parse(startDateStr, DATE_FMT);
        LocalDate endDate = LocalDate.parse(endDateStr, DATE_FMT);

        List<Map<String, Object>> rows = statMapper.trend(userId, gran, startDate, endDate);

        List<TrendItemVO> list = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            TrendItemVO item = new TrendItemVO();
            Object dateObj = row.get("statDate");
            if (dateObj != null) {
                item.setDate(dateObj.toString());
            }
            item.setIncome((BigDecimal) row.getOrDefault("income", BigDecimal.ZERO));
            item.setExpense((BigDecimal) row.getOrDefault("expense", BigDecimal.ZERO));
            list.add(item);
        }

        return Result.success(list);
    }

    @Override
    public Result familyMemberStats(Long userId, String startDateStr, String endDateStr) {
        LambdaQueryWrapper<FamilyMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FamilyMember::getUserId, userId);
        FamilyMember self = familyMemberMapper.selectOne(wrapper);

        if (self == null) {
            return Result.error("您未加入家庭组");
        }

        Long familyId = self.getFamilyId();
        boolean isAdmin = "ADMIN".equals(self.getRole());

        LocalDate startDate = LocalDate.parse(startDateStr, DATE_FMT);
        LocalDate endDate = LocalDate.parse(endDateStr, DATE_FMT);

        List<Map<String, Object>> rows = statMapper.familyMemberStats(familyId, startDate, endDate, userId, isAdmin);

        BigDecimal familyTotalIncome = BigDecimal.ZERO;
        BigDecimal familyTotalExpense = BigDecimal.ZERO;

        List<MemberStatItemVO> members = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            MemberStatItemVO item = new MemberStatItemVO();
            item.setUserId((Long) row.get("userId"));
            item.setNickname((String) row.get("nickname"));
            BigDecimal income = (BigDecimal) row.getOrDefault("totalIncome", BigDecimal.ZERO);
            BigDecimal expense = (BigDecimal) row.getOrDefault("totalExpense", BigDecimal.ZERO);
            item.setTotalIncome(income);
            item.setTotalExpense(expense);
            item.setBalance(income.subtract(expense));
            members.add(item);

            familyTotalIncome = familyTotalIncome.add(income);
            familyTotalExpense = familyTotalExpense.add(expense);
        }

        for (MemberStatItemVO item : members) {
            item.setIncomePercentage(familyTotalIncome.compareTo(BigDecimal.ZERO) > 0
                    ? item.getTotalIncome().multiply(new BigDecimal("100")).divide(familyTotalIncome, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);
            item.setExpensePercentage(familyTotalExpense.compareTo(BigDecimal.ZERO) > 0
                    ? item.getTotalExpense().multiply(new BigDecimal("100")).divide(familyTotalExpense, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);
        }

        FamilyMemberStatsVO vo = new FamilyMemberStatsVO();
        vo.setFamilyTotalIncome(familyTotalIncome);
        vo.setFamilyTotalExpense(familyTotalExpense);
        vo.setFamilyBalance(familyTotalIncome.subtract(familyTotalExpense));
        vo.setMembers(members);

        return Result.success(vo);
    }
}
