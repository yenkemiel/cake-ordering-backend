package com.kemiel.cakeordering.product.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 後台商品新增／編輯回應 DTO
 */
public class ProductResponse {

    private final Long id;
    private final String name;
    private final Long categoryId;
    private final String categoryName;
    private final String description;
    private final String imageUrl;
    private final List<ProductVariantSummaryResponse> variants;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ProductResponse(Long id, String name, Long categoryId, String categoryName, String description,
                           String imageUrl, List<ProductVariantSummaryResponse> variants,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.description = description;
        this.imageUrl = imageUrl;
        this.variants = variants;
        this.createdAt = createdAt;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
