package com.kemiel.cakeordering.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 編輯分類請求 DTO
 */
public class UpdateCategoryRequest {

    @NotBlank(message = "分類名稱不可為空")
    @Size(max = 50, message = "分類名稱長度不可超過 50 字")
    private String name;

    public UpdateCategoryRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
