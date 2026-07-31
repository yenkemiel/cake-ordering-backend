package com.kemiel.cakeordering.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/**
 * 建立訂單請求 DTO
 */
public class CreateOrderRequest {

    @NotBlank
    @Size(max = 50)
    private String customerName;

    @NotBlank
    @Pattern(regexp = "^09\\d{8}$")
    private String phone;

    @NotBlank
    @Email
    @Size(max = 100)
    private String email;

    @NotBlank
    @Pattern(regexp = "^(PICKUP|DELIVERY)$", message = "shippingMethod 須為 PICKUP 或 DELIVERY")
    private String shippingMethod;

    @NotBlank
    @Pattern(regexp = "^(ONLINE_PAYMENT|STORE_PAYMENT)$", message = "paymentMethod 須為 ONLINE_PAYMENT 或 STORE_PAYMENT")
    private String paymentMethod;

    @Size(max = 255)
    private String address;

    @NotNull
    private LocalDate pickupDate;

    @Size(max = 500)
    private String remark;

    @NotEmpty
    @Valid
    private List<OrderItemRequest> items;

    public CreateOrderRequest() {
    }

    @AssertTrue(message = "宅配時地址為必填")
    private boolean isAddressValidForDelivery() {
        if (!"DELIVERY".equals(shippingMethod)) {
            return true;
        }
        return address != null && !address.isBlank();
    }

    @AssertTrue(message = "宅配時付款方式僅能為線上刷卡")
    private boolean isPaymentMethodValidForDelivery() {
        if (!"DELIVERY".equals(shippingMethod)) {
            return true;
        }
        return "ONLINE_PAYMENT".equals(paymentMethod);
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

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
}