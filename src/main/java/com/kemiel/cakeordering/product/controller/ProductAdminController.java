package com.kemiel.cakeordering.product.controller;

import com.kemiel.cakeordering.common.response.ApiResponse;
import com.kemiel.cakeordering.common.response.PageResult;
import com.kemiel.cakeordering.product.dto.AdjustVariantStockRequest;
import com.kemiel.cakeordering.product.dto.CreateProductRequest;
import com.kemiel.cakeordering.product.dto.CreateVariantRequest;
import com.kemiel.cakeordering.product.dto.ProductResponse;
import com.kemiel.cakeordering.product.dto.ProductSummaryResponse;
import com.kemiel.cakeordering.product.dto.ProductVariantResponse;
import com.kemiel.cakeordering.product.dto.UpdateProductRequest;
import com.kemiel.cakeordering.product.dto.UpdateVariantRequest;
import com.kemiel.cakeordering.product.dto.UpdateVariantStatusRequest;
import com.kemiel.cakeordering.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品管理 Controller，提供後台商品與變體 CRUD，需 ADMIN 登入
 */
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@Tag(name = "Product Admin", description = "商品管理（需登入）")
public class ProductAdminController {

    private final ProductService productService;

    @Operation(summary = "[FR-PRD-001] 後台商品列表查詢")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResult<ProductSummaryResponse>>> list(
            @RequestParam(name = "categoryId", required = false) List<Long> categoryIds,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        PageResult<ProductSummaryResponse> result =
                productService.listProductsForAdmin(categoryIds, status, sort, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "[FR-PRD-004] 後台新增商品")
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> create(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(summary = "[FR-PRD-005] 後台編輯商品基本資料")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> update(@PathVariable Long id,
                                                               @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success(productService.updateProduct(id, request)));
    }

    @Operation(summary = "[FR-PRD-006] 後台刪除商品")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "[FR-PRD-007] 新增變體")
    @PostMapping("/{productId}/variants")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> createVariant(
            @PathVariable Long productId, @Valid @RequestBody CreateVariantRequest request) {
        ProductVariantResponse response = productService.createVariant(productId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(summary = "[FR-PRD-008] 編輯變體")
    @PutMapping("/{productId}/variants/{variantId}")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> updateVariant(
            @PathVariable Long productId, @PathVariable Long variantId,
            @Valid @RequestBody UpdateVariantRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.updateVariant(productId, variantId, request)));
    }

    @Operation(summary = "[FR-PRD-009] 刪除單一變體")
    @DeleteMapping("/{productId}/variants/{variantId}")
    public ResponseEntity<ApiResponse<Void>> deleteVariant(@PathVariable Long productId,
                                                           @PathVariable Long variantId) {
        productService.deleteVariant(productId, variantId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "[FR-PRD-010] 切換變體上下架")
    @PatchMapping("/{productId}/variants/{variantId}/status")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> updateVariantStatus(
            @PathVariable Long productId, @PathVariable Long variantId,
            @Valid @RequestBody UpdateVariantStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.updateVariantStatus(productId, variantId, request)));
    }

    @Operation(summary = "[FR-PRD-011] 變體庫存調整")
    @PatchMapping("/{productId}/variants/{variantId}/stock")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> adjustVariantStock(
            @PathVariable Long productId, @PathVariable Long variantId,
            @Valid @RequestBody AdjustVariantStockRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.adjustVariantStock(productId, variantId, request)));
    }
}
