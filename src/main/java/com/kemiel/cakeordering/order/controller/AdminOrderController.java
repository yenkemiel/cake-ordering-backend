package com.kemiel.cakeordering.order.controller;

import com.kemiel.cakeordering.common.response.ApiResponse;
import com.kemiel.cakeordering.common.response.PageResult;
import com.kemiel.cakeordering.order.dto.OrderDetailResponse;
import com.kemiel.cakeordering.order.dto.OrderSummaryResponse;
import com.kemiel.cakeordering.order.dto.UpdateOrderStatusRequest;
import com.kemiel.cakeordering.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 後台訂單管理 Controller，涵蓋訂單列表、明細、狀態切換三支 API
 */
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@Tag(name = "Order Admin", description = "後台訂單管理")
public class AdminOrderController {

    private final OrderService orderService;

    @Operation(summary = "[FR-ORD-003] 後台訂單列表")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResult<OrderSummaryResponse>>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        PageResult<OrderSummaryResponse> result = orderService.listOrdersForAdmin(status, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "[FR-ORD-004] 後台訂單明細")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderDetailForAdmin(id)));
    }

    @Operation(summary = "[FR-ORD-005] 後台更新訂單狀態")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> updateStatus(
            @PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(orderService.updateOrderStatus(id, request)));
    }
}