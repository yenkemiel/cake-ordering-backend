package com.kemiel.cakeordering.category.dto;

import java.time.LocalDateTime;

/**
 * 分類回應 DTO
 */
public class CategoryResponse {

    private final Long id;
    private final String name;
    private final LocalDateTime createdAt;

    public CategoryResponse(Long id, String name, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
