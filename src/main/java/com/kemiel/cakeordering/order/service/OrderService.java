package com.kemiel.cakeordering.order.service;

import com.kemiel.cakeordering.common.response.PageResult;
import com.kemiel.cakeordering.order.dto.CreateOrderRequest;
import com.kemiel.cakeordering.order.dto.OrderResponse;
import com.kemiel.cakeordering.order.dto.OrderSummaryResponse;
import com.kemiel.cakeordering.order.dto.QueryOrderRequest;
import com.kemiel.cakeordering.order.dto.UpdateOrderStatusRequest;

/**
 * 訂單 Service 介面
 */
public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    void sendOrderConfirmationEmail(OrderResponse order);

    OrderResponse queryOrder(QueryOrderRequest request);

    PageResult<OrderSummaryResponse> listOrdersForAdmin(String status, Integer page, Integer size);

    OrderResponse getOrderDetailForAdmin(Long id);

    OrderResponse updateOrderStatus(Long id, UpdateOrderStatusRequest request);
}