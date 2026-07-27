package com.kemiel.cakeordering.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * 編輯變體請求 DTO
 */
public class UpdateVariantRequest {

    @Size(max = 20, message = "尺寸長度不可超過 20 字")
    private String size;

    @NotNull(message = "價格不可為空")
    @DecimalMin(value = "0.0", inclusive = false, message = "價格須大於 0")
    private BigDecimal price;

    public UpdateVariantRequest() {
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}