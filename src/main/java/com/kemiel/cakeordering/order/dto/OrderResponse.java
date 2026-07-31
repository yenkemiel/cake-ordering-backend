package com.kemiel.cakeordering.order.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 訂單查詢回應 DTO，FR-ORD-001／FR-ORD-002 共用
 */
public class OrderResponse {

    private final String orderNo;
    private final String status;
    private final String customerName;
    private final String phone;
    private final String email;
    private final String shippingMethod;
    private final String paymentMethod;
    private final String address;
    private final LocalDate pickupDate;
    private final String remark;
    private final List<OrderItemResponse> items;
    private final BigDecimal shippingFee;
    private final BigDecimal totalAmount;
    private final LocalDateTime createdAt;

    public OrderResponse(String orderNo, String status, String customerName, String phone, String email,
                         String shippingMethod, String paymentMethod, String address, LocalDate pickupDate,
                         String remark, List<OrderItemResponse> items, BigDecimal shippingFee,
                         BigDecimal totalAmount, LocalDateTime createdAt) {
        this.orderNo = orderNo;
        this.status = status;
        this.customerName = customerName;
        this.phone = phone;
        this.email = email;
        this.shippingMethod = shippingMethod;
        this.paymentMethod = paymentMethod;
        this.address = address;
        this.pickupDate = pickupDate;
        this.remark = remark;
        this.items = items;
        this.shippingFee = shippingFee;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public String getStatus() {
        return status;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getAddress() {
        return address;
    }

    public LocalDate getPickupDate() {
        return pickupDate;
    }

    public String getRemark() {
        return remark;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}