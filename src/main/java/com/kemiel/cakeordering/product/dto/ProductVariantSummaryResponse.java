package com.kemiel.cakeordering.product.dto;

import java.math.BigDecimal;

/**
 * 後台用商品變體回應 DTO，含上下架狀態
 */
public class ProductVariantSummaryResponse {

    private final Long id;
    private final String size;
    private final BigDecimal price;
    private final Integer stock;
    private final String status;

    public ProductVariantSummaryResponse(Long id, String size, BigDecimal price, Integer stock, String status) {
        this.id = id;
        this.size = size;
        this.price = price;
        this.stock = stock;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getSize() {
        return size;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getStock() {
        return stock;
    }

    public String getStatus() {
        return status;
    }
}
