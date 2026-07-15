package com.kemiel.cakeordering.common.health;

import lombok.Builder;
import lombok.Getter;

/**
 * 健康檢查回應 DTO
 */
@Getter
@Builder
public class HealthResponse {
    private final String status;
    private final long categoriesCount;
}