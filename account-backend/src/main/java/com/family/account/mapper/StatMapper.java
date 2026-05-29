package com.family.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.account.entity.Bill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface StatMapper extends BaseMapper<Bill> {

    // 查询指定时间范围内的收支汇总
    @Select("SELECT " +
            "COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END), 0) AS totalIncome, " +
            "COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0) AS totalExpense " +
            "FROM bill " +
            "WHERE user_id = #{userId} AND date BETWEEN #{startDate} AND #{endDate}")
    Map<String, Object> selectSummaryByUserAndDate(@Param("userId") Long userId,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    // 查询家庭组指定时间范围内的收支汇总（管理员可看全部，普通成员只看到FAMILY可见的）
    @Select("SELECT " +
            "COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END), 0) AS totalIncome, " +
            "COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0) AS totalExpense " +
            "FROM bill " +
            "WHERE family_id = #{familyId} AND date BETWEEN #{startDate} AND #{endDate} " +
            "AND (visible = 'FAMILY' OR user_id = #{userId})")
    Map<String, Object> selectFamilySummary(@Param("familyId") Long familyId,
                                             @Param("userId") Long userId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    // 查询家庭组整体汇总（管理员专用，可看全部）
    @Select("SELECT " +
            "COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END), 0) AS totalIncome, " +
            "COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0) AS totalExpense " +
            "FROM bill " +
            "WHERE family_id = #{familyId} AND date BETWEEN #{startDate} AND #{endDate}")
    Map<String, Object> selectFamilySummaryAll(@Param("familyId") Long familyId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    // 查询指定时间范围内各分类的支出统计
    @Select("SELECT c.id AS categoryId, c.name AS categoryName, " +
            "COALESCE(SUM(b.amount), 0) AS amount, " +
            "c.type AS type " +
            "FROM bill b " +
            "JOIN category c ON b.category_id = c.id " +
            "WHERE b.user_id = #{userId} " +
            "AND b.type = #{type} " +
            "AND b.date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY c.id, c.name, c.type " +
            "ORDER BY amount DESC")
    List<Map<String, Object>> selectCategoryStat(@Param("userId") Long userId,
                                                  @Param("type") String type,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    // 查询指定时间范围内每天的收支趋势
    @Select("SELECT b.date AS date, " +
            "COALESCE(SUM(CASE WHEN b.type = 'INCOME' THEN b.amount ELSE 0 END), 0) AS income, " +
            "COALESCE(SUM(CASE WHEN b.type = 'EXPENSE' THEN b.amount ELSE 0 END), 0) AS expense " +
            "FROM bill b " +
            "WHERE b.user_id = #{userId} " +
            "AND b.date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY b.date " +
            "ORDER BY b.date ASC")
    List<Map<String, Object>> selectDailyTrend(@Param("userId") Long userId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    // 查询最近N条收支记录
    @Select("SELECT b.id, b.type, c.name AS categoryName, b.amount, b.date, b.note " +
            "FROM bill b " +
            "JOIN category c ON b.category_id = c.id " +
            "WHERE b.user_id = #{userId} " +
            "ORDER BY b.date DESC, b.create_time DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectRecentBills(@Param("userId") Long userId, @Param("limit") int limit);
}
