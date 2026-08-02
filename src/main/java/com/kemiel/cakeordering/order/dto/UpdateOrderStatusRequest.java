package com.kemiel.cakeordering.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 後台更新訂單狀態的請求格式，status 須為 SHIPPED／COMPLETED／CANCELLED 其一
 */
public class UpdateOrderStatusRequest {

    @NotBlank
    @Pattern(regexp = "SHIPPED|COMPLETED|CANCELLED", message = "status 須為 SHIPPED、COMPLETED 或 CANCELLED")
    private String status;

    public UpdateOrderStatusRequest() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}