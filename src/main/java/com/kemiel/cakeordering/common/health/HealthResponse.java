package com.kemiel.cakeordering.common.health;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HealthResponse {
    private final String status;
    private final long categoriesCount;
}