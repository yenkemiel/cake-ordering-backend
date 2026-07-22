package com.kemiel.cakeordering.category.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 分類排序請求 DTO
 */
public class ReorderCategoryRequest {

    @NotEmpty(message = "分類排序清單不可為空")
    private List<Long> categoryIds;

    public ReorderCategoryRequest() {
    }

    public List<Long> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }
}
