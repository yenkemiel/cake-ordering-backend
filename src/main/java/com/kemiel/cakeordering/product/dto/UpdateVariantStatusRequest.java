package com.kemiel.cakeordering.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 切換變體上下架請求 DTO
 */
public class UpdateVariantStatusRequest {

    @NotBlank(message = "上下架狀態不可為空")
    @Pattern(regexp = "ACTIVE|INACTIVE", message = "上下架狀態須為 ACTIVE 或 INACTIVE")
    private String status;

    public UpdateVariantStatusRequest() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}