package com.kemiel.cakeordering.order.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 後台訂單列表回應格式，僅含列表展示所需欄位，不含品項明細與聯絡資訊
 */
public class OrderSummaryResponse {

    private final Long id;
    private final String orderNo;
    private final String customerName;
    private final String status;
    private final LocalDate pickupDate;
    private final BigDecimal totalAmount;
    private final LocalDateTime createdAt;

    public OrderSummaryResponse(Long id, String orderNo, String customerName, String status,
                                LocalDate pickupDate, BigDecimal totalAmount, LocalDateTime createdAt) {
        this.id = id;
        this.orderNo = orderNo;
        this.customerName = customerName;
        this.status = status;
        this.pickupDate = pickupDate;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getStatus() {
        return status;
    }

    public LocalDate getPickupDate() {
        return pickupDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}