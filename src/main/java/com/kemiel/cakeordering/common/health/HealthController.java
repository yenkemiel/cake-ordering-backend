package com.kemiel.cakeordering.common.health;

import com.kemiel.cakeordering.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public ApiResponse<HealthResponse> check() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM categories", Long.class);
        HealthResponse healthResponse = HealthResponse.builder()
                .status("OK")
                .categoriesCount(count)
                .build();
        return ApiResponse.success(healthResponse);
    }
}