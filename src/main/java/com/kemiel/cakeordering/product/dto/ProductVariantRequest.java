package com.kemiel.cakeordering.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * 新增商品時，巢狀帶入的單筆變體資料 DTO
 */
public class ProductVariantRequest {

    @Size(max = 20, message = "尺寸長度不可超過 20 字")
    private String size;

    @NotNull(message = "價格不可為空")
    @DecimalMin(value = "0.0", inclusive = false, message = "價格須大於 0")
    private BigDecimal price;

    @NotNull(message = "庫存不可為空")
    @Min(value = 0, message = "庫存不可為負數")
    private Integer stock;

    @Pattern(regexp = "ACTIVE|INACTIVE", message = "上下架狀態須為 ACTIVE 或 INACTIVE")
    private String status;

    public ProductVariantRequest() {
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

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
