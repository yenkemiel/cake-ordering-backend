package com.kemiel.cakeordering.common.health;

/**
 * 健康檢查回應 DTO
 */
public class HealthResponse {
    private final String status;
    private final long categoriesCount;

    public HealthResponse(String status, long categoriesCount) {
        this.status = status;
        this.categoriesCount = categoriesCount;
    }

    public String getStatus() {
        return status;
    }

    public long getCategoriesCount() {
        return categoriesCount;
    }
}