package com.kemiel.cakeordering.product.controller;

import com.kemiel.cakeordering.common.response.ApiResponse;
import com.kemiel.cakeordering.common.response.PageResult;
import com.kemiel.cakeordering.product.dto.CreateProductRequest;
import com.kemiel.cakeordering.product.dto.ProductResponse;
import com.kemiel.cakeordering.product.dto.ProductSummaryResponse;
import com.kemiel.cakeordering.product.dto.UpdateProductRequest;
import com.kemiel.cakeordering.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品管理 Controller，提供後台商品 CRUD（不含變體），需 ADMIN 登入。
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
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        PageResult<ProductSummaryResponse> result =
                productService.listProductsForAdmin(categoryIds, sort, page, size);
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
}
