package com.kemiel.cakeordering.order.dto;

import java.math.BigDecimal;

/**
 * 訂單品項回應 DTO
 */
public class OrderItemResponse {

    private final String productName;
    private final String variantSize;
    private final Integer quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal subtotal;

    public OrderItemResponse(String productName, String variantSize, Integer quantity,
                             BigDecimal unitPrice, BigDecimal subtotal) {
        this.productName = productName;
        this.variantSize = variantSize;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

    public String getProductName() {
        return productName;
    }

    public String getVariantSize() {
        return variantSize;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}