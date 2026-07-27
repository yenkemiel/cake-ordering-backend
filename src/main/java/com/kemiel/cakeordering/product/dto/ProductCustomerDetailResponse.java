package com.kemiel.cakeordering.product.dto;

import java.util.List;

/**
 * 前台商品詳細回應 DTO
 */
public class ProductCustomerDetailResponse {

    private final Long id;
    private final String name;
    private final Long categoryId;
    private final String categoryName;
    private final String description;
    private final String imageUrl;
    private final List<ProductVariantCustomerResponse> variants;

    public ProductCustomerDetailResponse(Long id, String name, Long categoryId, String categoryName,
                                         String description, String imageUrl,
                                         List<ProductVariantCustomerResponse> variants) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public List<ProductVariantCustomerResponse> getVariants() {
        return variants;
    }
}
