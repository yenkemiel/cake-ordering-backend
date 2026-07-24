package com.kemiel.cakeordering.category.service;

import com.kemiel.cakeordering.category.dto.CategoryResponse;
import com.kemiel.cakeordering.category.dto.CategorySummaryResponse;
import com.kemiel.cakeordering.category.dto.CreateCategoryRequest;
import com.kemiel.cakeordering.category.dto.ReorderCategoryRequest;
import com.kemiel.cakeordering.category.dto.UpdateCategoryRequest;

import java.util.List;

/**
 * 分類 Service 介面
 */
public interface CategoryService {

    List<CategorySummaryResponse> listCategories();

    CategoryResponse createCategory(CreateCategoryRequest request);

    CategoryResponse updateCategory(Long id, UpdateCategoryRequest request);

    void deleteCategory(Long id);

    void reorderCategories(ReorderCategoryRequest request);
}
