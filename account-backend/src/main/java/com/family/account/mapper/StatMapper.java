package com.family.account.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface StatMapper {

    @Select("<script>" +
            "SELECT " +
            "  COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END), 0) AS totalIncome, " +
            "  COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0) AS totalExpense " +
            "FROM bill " +
            "WHERE user_id IN <foreach collection='userIds' item='uid' open='(' separator=',' close=')'>#{uid}</foreach>" +
            "  AND date BETWEEN #{startDate} AND #{endDate}" +
            "</script>")
    Map<String, BigDecimal> summary(@Param("userIds") List<Long> userIds,
                                    @Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);

    @Select("<script>" +
            "SELECT " +
            "  c.id AS categoryId, " +
            "  c.name AS categoryName, " +
            "  COALESCE(SUM(b.amount), 0) AS amount " +
            "FROM category c " +
            "LEFT JOIN bill b ON c.id = b.category_id " +
            "  AND b.user_id IN <foreach collection='userIds' item='uid' open='(' separator=',' close=')'>#{uid}</foreach>" +
            "  AND b.date BETWEEN #{startDate} AND #{endDate} " +
            "WHERE c.type = #{type} " +
            "GROUP BY c.id, c.name " +
            "HAVING SUM(b.amount) > 0 " +
            "ORDER BY amount DESC" +
            "</script>")
    List<Map<String, Object>> categoryStats(@Param("userIds") List<Long> userIds,
                                            @Param("type") String type,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    @Select("<script>" +
            "SELECT " +
            "  <choose>" +
            "    <when test='granularity == \"DAY\"'>b.date</when>" +
            "    <when test='granularity == \"WEEK\"'>DATE_SUB(b.date, INTERVAL WEEKDAY(b.date) DAY)</when>" +
            "    <when test='granularity == \"MONTH\"'>DATE_FORMAT(b.date, '%Y-%m-01')</when>" +
            "  </choose> AS statDate, " +
            "  COALESCE(SUM(CASE WHEN b.type = 'INCOME' THEN b.amount ELSE 0 END), 0) AS income, " +
            "  COALESCE(SUM(CASE WHEN b.type = 'EXPENSE' THEN b.amount ELSE 0 END), 0) AS expense " +
            "FROM bill b " +
            "WHERE b.user_id IN <foreach collection='userIds' item='uid' open='(' separator=',' close=')'>#{uid}</foreach>" +
            "  AND b.date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY statDate " +
            "ORDER BY statDate" +
            "</script>")
    List<Map<String, Object>> trend(@Param("userIds") List<Long> userIds,
                                    @Param("granularity") String granularity,
                                    @Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);

    @Select("<script>" +
            "SELECT " +
            "  fm.user_id AS userId, " +
            "  u.nickname AS nickname, " +
            "  COALESCE(SUM(CASE WHEN b.type = 'INCOME' THEN b.amount ELSE 0 END), 0) AS totalIncome, " +
            "  COALESCE(SUM(CASE WHEN b.type = 'EXPENSE' THEN b.amount ELSE 0 END), 0) AS totalExpense " +
            "FROM family_member fm " +
            "JOIN user u ON fm.user_id = u.id " +
            "LEFT JOIN bill b ON fm.user_id = b.user_id " +
            "  AND b.date BETWEEN #{startDate} AND #{endDate} " +
            "  <if test='!isAdmin'>" +
            "    AND (b.user_id = #{currentUserId} OR b.visible = 'FAMILY') " +
            "  </if>" +
            "WHERE fm.family_id = #{familyId} " +
            "GROUP BY fm.user_id, u.nickname " +
            "ORDER BY totalIncome DESC" +
            "</script>")
    List<Map<String, Object>> familyMemberStats(@Param("familyId") Long familyId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate,
                                                @Param("currentUserId") Long currentUserId,
                                                @Param("isAdmin") boolean isAdmin);

    // ========== 以下方法来自 feature/dashboard 分支 ==========

    /**
     * 查询家庭组整体汇总（管理员专用，可看全部）
     */
    @Select("SELECT " +
            "COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END), 0) AS totalIncome, " +
            "COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0) AS totalExpense " +
            "FROM bill " +
            "WHERE family_id = #{familyId} AND date BETWEEN #{startDate} AND #{endDate}")
    Map<String, Object> selectFamilySummaryAll(@Param("familyId") Long familyId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    /**
     * 查询最近N条收支记录
     */
    @Select("SELECT b.id, b.type, c.name AS categoryName, b.amount, b.date, b.note " +
            "FROM bill b " +
            "JOIN category c ON b.category_id = c.id " +
            "WHERE b.user_id = #{userId} " +
            "ORDER BY b.date DESC, b.create_time DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectRecentBills(@Param("userId") Long userId, @Param("limit") int limit);
}
