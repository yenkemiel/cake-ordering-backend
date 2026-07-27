package com.kemiel.cakeordering.product.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

/**
 * 調整變體庫存請求 DTO
 */
public class AdjustVariantStockRequest {

    @NotNull(message = "調整數量不可為空")
    private Integer quantity;

    @AssertTrue(message = "調整數量不可為 0")
    private boolean isQuantityValid() {
        return quantity == null || quantity != 0;
    }

    public AdjustVariantStockRequest() {
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}