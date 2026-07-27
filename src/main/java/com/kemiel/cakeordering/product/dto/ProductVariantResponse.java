package com.kemiel.cakeordering.product.dto;

import java.math.BigDecimal;

/**
 * 變體回應 DTO
 */
public class ProductVariantResponse {

    private final Long id;
    private final Long productId;
    private final String size;
    private final BigDecimal price;
    private final Integer stock;
    private final String status;

    public ProductVariantResponse(Long id, Long productId, String size, BigDecimal price, Integer stock,
                                  String status) {
        this.id = id;
        this.productId = productId;
        this.size = size;
        this.price = price;
        this.stock = stock;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
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