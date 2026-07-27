package com.kemiel.cakeordering.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 編輯商品基本資料請求 DTO
 */
public class UpdateProductRequest {

    @NotBlank(message = "商品名稱不可為空")
    @Size(max = 100, message = "商品名稱長度不可超過 100 字")
    private String name;

    @NotNull(message = "分類不可為空")
    private Long categoryId;

    private String description;

    @Size(max = 255, message = "圖片網址長度不可超過 255 字")
    private String imageUrl;

    public UpdateProductRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
