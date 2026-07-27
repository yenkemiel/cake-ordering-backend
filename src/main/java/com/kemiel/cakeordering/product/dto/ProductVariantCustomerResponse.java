package com.kemiel.cakeordering.product.dto;

import java.math.BigDecimal;

/**
 * 前台用商品變體回應 DTO，不含上下架狀態
 */
public class ProductVariantCustomerResponse {

    private final Long id;
    private final String size;
    private final BigDecimal price;
    private final Integer stock;

    public ProductVariantCustomerResponse(Long id, String size, BigDecimal price, Integer stock) {
        this.id = id;
        this.size = size;
        this.price = price;
        this.stock = stock;
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
}
