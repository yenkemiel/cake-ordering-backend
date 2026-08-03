package com.kemiel.cakeordering.product.controller;

import com.kemiel.cakeordering.common.response.ApiResponse;
import com.kemiel.cakeordering.common.response.PageResult;
import com.kemiel.cakeordering.product.dto.ProductCustomerDetailResponse;
import com.kemiel.cakeordering.product.dto.ProductCustomerSummaryResponse;
import com.kemiel.cakeordering.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品查詢 Controller，供前台免登入瀏覽商品
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "商品查詢（免登入）")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "[FR-PRD-002] 前台商品列表查詢")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResult<ProductCustomerSummaryResponse>>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long excludeCategoryId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        PageResult<ProductCustomerSummaryResponse> result =
                productService.listProductsForCustomer(categoryId, excludeCategoryId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "[FR-PRD-003] 前台商品詳細查詢")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductCustomerDetailResponse>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductDetail(id)));
    }
}