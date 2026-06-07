package com.family.account.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.family.account.common.Result;
import com.family.account.dto.category.*;
import com.family.account.entity.*;
import com.family.account.mapper.*;
import com.family.account.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private FamilyMemberMapper familyMemberMapper;

    @Autowired
    private CategoryApplyMapper categoryApplyMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BillMapper billMapper;

    // 获取用户所在家庭组成员记录
    private FamilyMember getFamilyMember(Long userId) {
        LambdaQueryWrapper<FamilyMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FamilyMember::getUserId, userId);
        return familyMemberMapper.selectOne(wrapper);
    }

    @Override
    public Result getCategoryList(Long userId) {
        FamilyMember member = getFamilyMember(userId);

        // 查询系统内置分类
        LambdaQueryWrapper<Category> systemWrapper = new LambdaQueryWrapper<>();
        systemWrapper.eq(Category::getIsSystem, 1);
        List<Category> systemCategories = categoryMapper.selectList(systemWrapper);

        // 查询用户个人分类
        LambdaQueryWrapper<Category> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(Category::getUserId, userId)
                .eq(Category::getIsSystem, 0);
        List<Category> userCategories = categoryMapper.selectList(userWrapper);

        // 如果是家庭成员，额外查询家庭分类
        List<Category> familyCategories = new ArrayList<>();
        if (member != null) {
            LambdaQueryWrapper<Category> familyWrapper = new LambdaQueryWrapper<>();
            familyWrapper.eq(Category::getFamilyId, member.getFamilyId())
                    .eq(Category::getIsSystem, 0);
            familyCategories = categoryMapper.selectList(familyWrapper);
        }

        // 合并去重：系统分类 + 个人分类 + 家庭分类
        // 使用LinkedHashMap按插入顺序去重，以id为key
        Map<Long, Category> merged = new LinkedHashMap<>();
        for (Category c : systemCategories) {
            merged.put(c.getId(), c);
        }
        for (Category c : userCategories) {
            merged.put(c.getId(), c);
        }
        for (Category c : familyCategories) {
            merged.put(c.getId(), c);
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Category c : merged.values()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", c.getId());
            item.put("name", c.getName());
            item.put("type", c.getType());
            item.put("isSystem", c.getIsSystem() == 1);
            list.add(item);
        }

        return Result.success(list);
    }

    @Override
    public Result addCategory(Long userId, CategoryAddDTO dto) {
        FamilyMember member = getFamilyMember(userId);

        // 检查分类名称是否已存在（同类型下）
        LambdaQueryWrapper<Category> nameCheck = new LambdaQueryWrapper<>();
        nameCheck.eq(Category::getName, dto.getName())
                .eq(Category::getType, dto.getType());
        if (categoryMapper.selectOne(nameCheck) != null) {
            return Result.error("该分类名称已存在");
        }

        if (member == null) {
            // 独立用户：直接新增个人分类
            Category category = new Category();
            category.setName(dto.getName());
            category.setType(dto.getType());
            category.setIsSystem(0);
            category.setUserId(userId);
            categoryMapper.insert(category);

            Map<String, Object> data = new HashMap<>();
            data.put("id", category.getId());
            return Result.success(data);
        }

        // 家庭管理员：直接新增家庭分类，无需审核，立即对全体成员生效
        if (member.getRole().equals("ADMIN")) {
            Category category = new Category();
            category.setName(dto.getName());
            category.setType(dto.getType());
            category.setIsSystem(0);
            category.setFamilyId(member.getFamilyId());
            categoryMapper.insert(category);

            Map<String, Object> data = new HashMap<>();
            data.put("id", category.getId());
            return Result.success(data);
        }

        // 普通家庭成员：提交分类申请，进入审核流程
        // 检查是否已有待审核的同名申请
        LambdaQueryWrapper<CategoryApply> applyCheck = new LambdaQueryWrapper<>();
        applyCheck.eq(CategoryApply::getFamilyId, member.getFamilyId())
                .eq(CategoryApply::getUserId, userId)
                .eq(CategoryApply::getCategoryName, dto.getName())
                .eq(CategoryApply::getStatus, 0);
        if (categoryApplyMapper.selectOne(applyCheck) != null) {
            return Result.error("您已提交过同名分类申请，请等待审核");
        }

        CategoryApply apply = new CategoryApply();
        apply.setFamilyId(member.getFamilyId());
        apply.setUserId(userId);
        apply.setCategoryName(dto.getName());
        apply.setType(dto.getType());
        apply.setStatus(0);
        categoryApplyMapper.insert(apply);

        Map<String, Object> data = new HashMap<>();
        data.put("id", apply.getId());
        return Result.success(data);
    }

    @Override
    public Result updateCategory(Long userId, CategoryUpdateDTO dto) {
        Category category = categoryMapper.selectById(dto.getId());
        if (category == null) {
            return Result.error("分类不存在");
        }
        if (category.getIsSystem() == 1) {
            return Result.error("系统内置分类不可修改");
        }

        // 检查权限：只能修改自己的个人分类，或家庭管理员可修改家庭分类
        FamilyMember member = getFamilyMember(userId);
        boolean canModify = category.getUserId() != null && category.getUserId().equals(userId);
        if (!canModify && member != null && member.getRole().equals("ADMIN")
                && category.getFamilyId() != null && category.getFamilyId().equals(member.getFamilyId())) {
            canModify = true;
        }
        if (!canModify) {
            return Result.error(403, "无权限修改该分类");
        }

        // 检查新名称是否与其他分类冲突
        LambdaQueryWrapper<Category> nameCheck = new LambdaQueryWrapper<>();
        nameCheck.eq(Category::getName, dto.getName())
                .eq(Category::getType, category.getType())
                .ne(Category::getId, dto.getId());
        if (categoryMapper.selectOne(nameCheck) != null) {
            return Result.error("该分类名称已存在");
        }

        category.setName(dto.getName());
        categoryMapper.updateById(category);
        return Result.success();
    }

    @Override
    public Result deleteCategory(Long userId, Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            return Result.error("分类不存在");
        }
        if (category.getIsSystem() == 1) {
            return Result.error("系统内置分类不可删除");
        }

        // 检查权限
        FamilyMember member = getFamilyMember(userId);
        boolean canDelete = category.getUserId() != null && category.getUserId().equals(userId);
        if (!canDelete && member != null && member.getRole().equals("ADMIN")
                && category.getFamilyId() != null && category.getFamilyId().equals(member.getFamilyId())) {
            canDelete = true;
        }
        if (!canDelete) {
            return Result.error(403, "无权限删除该分类");
        }

        // 检查分类是否已被使用
        LambdaQueryWrapper<Bill> billCheck = new LambdaQueryWrapper<>();
        billCheck.eq(Bill::getCategoryId, categoryId);
        if (billMapper.selectCount(billCheck) > 0) {
            return Result.error("该分类已被使用，请先迁移关联记录");
        }

        categoryMapper.deleteById(categoryId);
        return Result.success();
    }

    @Override
    public Result migrateCategory(Long userId, CategoryMigrateDTO dto) {
        if (dto.getFromCategoryId().equals(dto.getToCategoryId())) {
            return Result.error("源分类和目标分类不能相同");
        }

        Category fromCategory = categoryMapper.selectById(dto.getFromCategoryId());
        if (fromCategory == null) {
            return Result.error("源分类不存在");
        }
        Category toCategory = categoryMapper.selectById(dto.getToCategoryId());
        if (toCategory == null) {
            return Result.error("目标分类不存在");
        }

        // 检查权限：只能操作自己的个人分类，或家庭管理员操作家庭分类
        FamilyMember member = getFamilyMember(userId);
        boolean canMigrate = fromCategory.getUserId() != null && fromCategory.getUserId().equals(userId);
        if (!canMigrate && member != null && member.getRole().equals("ADMIN")
                && fromCategory.getFamilyId() != null && fromCategory.getFamilyId().equals(member.getFamilyId())) {
            canMigrate = true;
        }
        if (!canMigrate) {
            return Result.error(403, "无权限操作该分类");
        }

        // 迁移记录
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Bill> updateWrapper =
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        updateWrapper.eq(Bill::getCategoryId, dto.getFromCategoryId())
                .set(Bill::getCategoryId, dto.getToCategoryId());
        billMapper.update(null, updateWrapper);

        return Result.success();
    }

    @Override
    public Result getApplyList(Long userId) {
        FamilyMember member = getFamilyMember(userId);
        if (member == null || !member.getRole().equals("ADMIN")) {
            return Result.error(403, "无权限");
        }

        LambdaQueryWrapper<CategoryApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CategoryApply::getFamilyId, member.getFamilyId())
                .eq(CategoryApply::getStatus, 0);
        List<CategoryApply> applies = categoryApplyMapper.selectList(wrapper);

        List<Map<String, Object>> list = new ArrayList<>();
        for (CategoryApply apply : applies) {
            User user = userMapper.selectById(apply.getUserId());
            Map<String, Object> item = new HashMap<>();
            item.put("applyId", apply.getId());
            item.put("userId", apply.getUserId());
            item.put("nickname", user.getNickname());
            item.put("categoryName", apply.getCategoryName());
            item.put("type", apply.getType());
            item.put("applyTime", apply.getApplyTime());
            list.add(item);
        }

        return Result.success(list);
    }

    @Override
    public Result reviewApply(Long userId, CategoryReviewDTO dto) {
        FamilyMember member = getFamilyMember(userId);
        if (member == null || !member.getRole().equals("ADMIN")) {
            return Result.error(403, "无权限");
        }

        CategoryApply apply = categoryApplyMapper.selectById(dto.getApplyId());
        if (apply == null || apply.getStatus() != 0) {
            return Result.error("申请不存在或已处理");
        }

        if (dto.getApprove()) {
            if (dto.getMergeToCategoryId() != null) {
                // 合并到已有分类
                Category targetCategory = categoryMapper.selectById(dto.getMergeToCategoryId());
                if (targetCategory == null) {
                    return Result.error("目标分类不存在");
                }
                apply.setMergeToCategoryId(dto.getMergeToCategoryId());
            } else {
                // 独立新增为家庭分类
                // 检查同名分类是否已存在
                LambdaQueryWrapper<Category> nameCheck = new LambdaQueryWrapper<>();
                nameCheck.eq(Category::getName, apply.getCategoryName())
                        .eq(Category::getType, apply.getType());
                if (categoryMapper.selectOne(nameCheck) != null) {
                    return Result.error("该分类名称已存在，请选择合并到已有分类");
                }

                Category newCategory = new Category();
                newCategory.setName(apply.getCategoryName());
                newCategory.setType(apply.getType());
                newCategory.setIsSystem(0);
                newCategory.setFamilyId(member.getFamilyId());
                categoryMapper.insert(newCategory);
            }
        }

        apply.setStatus(dto.getApprove() ? 1 : 2);
        apply.setReviewTime(LocalDateTime.now());
        categoryApplyMapper.updateById(apply);

        return Result.success();
    }
}
