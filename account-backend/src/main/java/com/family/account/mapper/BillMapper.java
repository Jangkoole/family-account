package com.family.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.family.account.entity.Bill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;

@Mapper
public interface BillMapper extends BaseMapper<Bill> {

    /**
     * 个人账单分页查询（含可见性过滤）
     */
    @Select("<script>" +
            "SELECT b.*, c.name AS categoryName " +
            "FROM bill b " +
            "LEFT JOIN category c ON b.category_id = c.id " +
            "WHERE (b.user_id = #{userId} " +
            "  <if test='familyId != null'>" +
            "    OR (b.family_id = #{familyId} AND b.visible = 'FAMILY')" +
            "  </if>" +
            ")" +
            "  <if test='startDate != null'>AND b.date &gt;= #{startDate}</if>" +
            "  <if test='endDate != null'>AND b.date &lt;= #{endDate}</if>" +
            "  <if test='type != null and type != \"\"'>AND b.type = #{type}</if>" +
            "  <if test='categoryId != null'>AND b.category_id = #{categoryId}</if>" +
            "  <if test='visible != null and visible != \"\"'>AND b.visible = #{visible}</if>" +
            "  <if test='minAmount != null'>AND b.amount &gt;= #{minAmount}</if>" +
            "  <if test='maxAmount != null'>AND b.amount &lt;= #{maxAmount}</if>" +
            "ORDER BY b.date DESC, b.create_time DESC" +
            "</script>")
    Page<Bill> selectBillPage(Page<Bill> page,
                              @Param("userId") Long userId,
                              @Param("familyId") Long familyId,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate,
                              @Param("type") String type,
                              @Param("categoryId") Long categoryId,
                              @Param("visible") String visible,
                              @Param("minAmount") BigDecimal minAmount,
                              @Param("maxAmount") BigDecimal maxAmount);

    /**
     * 家庭管理员查询所有成员记录（无视可见性）
     */
    @Select("<script>" +
            "SELECT b.*, c.name AS categoryName, u.nickname " +
            "FROM bill b " +
            "LEFT JOIN category c ON b.category_id = c.id " +
            "LEFT JOIN user u ON b.user_id = u.id " +
            "WHERE b.family_id = #{familyId}" +
            "  <if test='userId != null'>AND b.user_id = #{userId}</if>" +
            "  <if test='startDate != null'>AND b.date &gt;= #{startDate}</if>" +
            "  <if test='endDate != null'>AND b.date &lt;= #{endDate}</if>" +
            "  <if test='type != null and type != \"\"'>AND b.type = #{type}</if>" +
            "  <if test='categoryId != null'>AND b.category_id = #{categoryId}</if>" +
            "ORDER BY b.date DESC, b.create_time DESC" +
            "</script>")
    Page<Bill> selectFamilyBillPage(Page<Bill> page,
                                    @Param("familyId") Long familyId,
                                    @Param("userId") Long userId,
                                    @Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate,
                                    @Param("type") String type,
                                    @Param("categoryId") Long categoryId);

    /**
     * 查询记录详情（含分类名称）
     */
    @Select("SELECT b.*, c.name AS categoryName " +
            "FROM bill b " +
            "LEFT JOIN category c ON b.category_id = c.id " +
            "WHERE b.id = #{id}")
    Bill selectDetailById(@Param("id") Long id);

    /**
     * 根据用户ID获取其所属家庭组ID（从 family_member 表）
     */
    @Select("SELECT family_id FROM family_member WHERE user_id = #{userId} LIMIT 1")
    Long selectFamilyIdByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID获取其在家庭组中的角色（从 family_member 表）
     */
    @Select("SELECT role FROM family_member WHERE user_id = #{userId} LIMIT 1")
    String selectFamilyRoleByUserId(@Param("userId") Long userId);
}
