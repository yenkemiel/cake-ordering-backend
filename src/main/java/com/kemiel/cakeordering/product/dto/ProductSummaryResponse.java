package com.kemiel.cakeordering.product.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 後台商品列表回應 DTO
 */
public class ProductSummaryResponse {

    private final Long id;
    private final String name;
    private final Long categoryId;
    private final String categoryName;
    private final String description;
    private final String imageUrl;
    private final List<ProductVariantSummaryResponse> variants;
    private final LocalDateTime updatedAt;

    public ProductSummaryResponse(Long id, String name, Long categoryId, String categoryName, String description,
                                  String imageUrl, List<ProductVariantSummaryResponse> variants,
                                  LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.description = description;
        this.imageUrl = imageUrl;
        this.variants = variants;
        this.updatedAt = updatedAt;
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

    public List<ProductVariantSummaryResponse> getVariants() {
        return variants;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
