package com.family.account.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.family.account.common.Result;
import com.family.account.dto.category.*;
import com.family.account.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // 2.1 获取分类列表
    @GetMapping("/list")
    public Result getCategoryList() {
        Long userId = StpUtil.getLoginIdAsLong();
        return categoryService.getCategoryList(userId);
    }

    // 2.2 新增自定义分类
    @PostMapping("/add")
    public Result addCategory(@Valid @RequestBody CategoryAddDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        return categoryService.addCategory(userId, dto);
    }

    // 2.3 修改自定义分类
    @PutMapping("/update")
    public Result updateCategory(@Valid @RequestBody CategoryUpdateDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        return categoryService.updateCategory(userId, dto);
    }

    // 2.4 删除自定义分类
    @DeleteMapping("/delete/{id}")
    public Result deleteCategory(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        return categoryService.deleteCategory(userId, id);
    }

    // 2.5 迁移分类下的记录
    @PutMapping("/migrate")
    public Result migrateCategory(@Valid @RequestBody CategoryMigrateDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        return categoryService.migrateCategory(userId, dto);
    }

    // 2.6 获取待审核分类申请列表
    @GetMapping("/apply/list")
    public Result getApplyList() {
        Long userId = StpUtil.getLoginIdAsLong();
        return categoryService.getApplyList(userId);
    }

    // 2.7 审核分类申请
    @PutMapping("/apply/review")
    public Result reviewApply(@Valid @RequestBody CategoryReviewDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        return categoryService.reviewApply(userId, dto);
    }
}
