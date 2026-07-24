package com.kemiel.cakeordering.category.controller;

import com.kemiel.cakeordering.category.dto.CategoryResponse;
import com.kemiel.cakeordering.category.dto.CreateCategoryRequest;
import com.kemiel.cakeordering.category.dto.ReorderCategoryRequest;
import com.kemiel.cakeordering.category.dto.UpdateCategoryRequest;
import com.kemiel.cakeordering.category.service.CategoryService;
import com.kemiel.cakeordering.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 分類管理 Controller，提供後台分類新增／編輯／刪除／排序，需 ADMIN 登入
 */
@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@Tag(name = "Category Admin", description = "分類管理（需登入）")
public class CategoryAdminController {

    private final CategoryService categoryService;

    @Operation(summary = "[FR-CAT-002] 新增分類")
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(summary = "[FR-CAT-003] 編輯分類")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(@PathVariable Long id,
                                                                @Valid @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.updateCategory(id, request)));
    }

    @Operation(summary = "[FR-CAT-004] 刪除分類")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "[FR-CAT-005] 調整分類排序")
    @PatchMapping("/reorder")
    public ResponseEntity<ApiResponse<Void>> reorder(@Valid @RequestBody ReorderCategoryRequest request) {
        categoryService.reorderCategories(request);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
