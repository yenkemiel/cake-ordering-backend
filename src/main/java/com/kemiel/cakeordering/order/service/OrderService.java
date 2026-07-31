package com.kemiel.cakeordering.order.service;

import com.kemiel.cakeordering.order.dto.CreateOrderRequest;
import com.kemiel.cakeordering.order.dto.OrderResponse;
import com.kemiel.cakeordering.order.dto.QueryOrderRequest;

/**
 * 訂單 Service 介面
 */
public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    void sendOrderConfirmationEmail(OrderResponse order);

    OrderResponse queryOrder(QueryOrderRequest request);
}