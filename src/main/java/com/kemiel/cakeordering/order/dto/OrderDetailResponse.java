package com.kemiel.cakeordering.order.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 後台訂單明細回應格式，內容同訪客端 OrderResponse 並額外含資料庫 id，
 * 供 FR-ORD-004（訂單明細）／FR-ORD-005（狀態更新）使用
 */
public class OrderDetailResponse {

    private Long id;
    private String orderNo;
    private String status;
    private String customerName;
    private String phone;
    private String email;
    private String shippingMethod;
    private String paymentMethod;
    private String address;
    private LocalDate pickupDate;
    private String remark;
    private List<OrderItemResponse> items;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;

    public OrderDetailResponse() {
    }

    public OrderDetailResponse(Long id, String orderNo, String status, String customerName, String phone,
                               String email, String shippingMethod, String paymentMethod, String address,
                               LocalDate pickupDate, String remark, List<OrderItemResponse> items,
                               BigDecimal shippingFee, BigDecimal totalAmount, LocalDateTime createdAt) {
        this.id = id;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(LocalDate pickupDate) {
        this.pickupDate = pickupDate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }

    public void setItems(List<OrderItemResponse> items) {
        this.items = items;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}