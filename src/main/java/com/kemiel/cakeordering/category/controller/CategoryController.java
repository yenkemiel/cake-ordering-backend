package com.kemiel.cakeordering.category.controller;

import com.kemiel.cakeordering.category.dto.CategorySummaryResponse;
import com.kemiel.cakeordering.category.service.CategoryService;
import com.kemiel.cakeordering.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分類查詢 Controller，供前台與後台共用免登入的分類清單查詢
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "分類查詢（免登入）")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "[FR-CAT-001] 查詢分類列表")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategorySummaryResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.listCategories()));
    }
}