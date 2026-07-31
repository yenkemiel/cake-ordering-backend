package com.kemiel.cakeordering.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 建立訂單請求內的品項片段 DTO
 */
public class OrderItemRequest {

    @NotNull
    private Long variantId;

    @NotNull
    @Positive
    private Integer quantity;

    public OrderItemRequest() {
    }

    public Long getVariantId() {
        return variantId;
    }

    public void setVariantId(Long variantId) {
        this.variantId = variantId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}