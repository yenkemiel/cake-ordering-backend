package com.kemiel.cakeordering.order.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 訪客訂單查詢請求 DTO
 */
public class QueryOrderRequest {

    @NotBlank
    private String orderNo;

    @NotBlank
    private String phone;

    public QueryOrderRequest() {
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}