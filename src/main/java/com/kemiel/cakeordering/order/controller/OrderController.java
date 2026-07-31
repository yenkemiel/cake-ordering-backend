package com.kemiel.cakeordering.order.controller;

import com.kemiel.cakeordering.common.response.ApiResponse;
import com.kemiel.cakeordering.order.dto.CreateOrderRequest;
import com.kemiel.cakeordering.order.dto.OrderResponse;
import com.kemiel.cakeordering.order.dto.QueryOrderRequest;
import com.kemiel.cakeordering.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 訂單 Controller，提供訪客建立訂單與查詢訂單 API
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "訂單管理（免登入）")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "[FR-ORD-001] 建立訂單")
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        orderService.sendOrderConfirmationEmail(response);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(summary = "[FR-ORD-002] 訪客訂單查詢")
    @PostMapping("/query")
    public ResponseEntity<ApiResponse<OrderResponse>> queryOrder(@Valid @RequestBody QueryOrderRequest request) {
        OrderResponse response = orderService.queryOrder(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}