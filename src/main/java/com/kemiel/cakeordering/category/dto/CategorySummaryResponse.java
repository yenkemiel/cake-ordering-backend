package com.kemiel.cakeordering.category.dto;

/**
 * 分類列表回應 DTO
 */
public class CategorySummaryResponse {

    private final Long id;
    private final String name;

    public CategorySummaryResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
