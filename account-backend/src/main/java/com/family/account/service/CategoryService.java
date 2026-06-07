package com.family.account.service;

import com.family.account.common.Result;
import com.family.account.dto.category.*;

public interface CategoryService {

    Result getCategoryList(Long userId);

    Result addCategory(Long userId, CategoryAddDTO dto);

    Result updateCategory(Long userId, CategoryUpdateDTO dto);

    Result deleteCategory(Long userId, Long categoryId);

    Result migrateCategory(Long userId, CategoryMigrateDTO dto);

    Result getApplyList(Long userId);

    Result reviewApply(Long userId, CategoryReviewDTO dto);
}
