package com.kemiel.cakeordering.product.dto;

import java.util.List;

/**
 * 前台商品列表回應 DTO
 */
public class ProductCustomerSummaryResponse {

    private final Long id;
    private final String name;
    private final Long categoryId;
    private final String categoryName;
    private final String imageUrl;
    private final List<ProductVariantCustomerResponse> variants;

    public ProductCustomerSummaryResponse(Long id, String name, Long categoryId, String categoryName,
                                          String imageUrl, List<ProductVariantCustomerResponse> variants) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.imageUrl = imageUrl;
        this.variants = variants;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public List<ProductVariantCustomerResponse> getVariants() {
        return variants;
    }
}