package com.kemiel.cakeordering.order.service;

import com.kemiel.cakeordering.common.exception.BusinessException;
import com.kemiel.cakeordering.common.exception.ErrorCode;
import com.kemiel.cakeordering.order.dto.CreateOrderRequest;
import com.kemiel.cakeordering.order.dto.OrderItemRequest;
import com.kemiel.cakeordering.order.dto.OrderItemResponse;
import com.kemiel.cakeordering.order.dto.OrderResponse;
import com.kemiel.cakeordering.order.dto.QueryOrderRequest;
import com.kemiel.cakeordering.order.email.EmailApiClient;
import com.kemiel.cakeordering.order.entity.Order;
import com.kemiel.cakeordering.order.entity.OrderItem;
import com.kemiel.cakeordering.order.repository.OrderRepository;
import com.kemiel.cakeordering.product.entity.Product;
import com.kemiel.cakeordering.product.entity.ProductVariant;
import com.kemiel.cakeordering.product.repository.ProductRepository;
import com.kemiel.cakeordering.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 訂單 Service 實作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final int ORDER_NO_RANDOM_BOUND = 10000;

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final EmailApiClient emailApiClient;

    /**
     * 建立訂單，含金額重算、樂觀鎖扣庫存；不包含 Email 通知信呼叫
     */
    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        BigDecimal shippingFee = "DELIVERY".equals(request.getShippingMethod())
                ? BigDecimal.valueOf(250)
                : BigDecimal.ZERO;

        List<OrderItem> items = new ArrayList<>();
        BigDecimal totalAmount = shippingFee;

        for (OrderItemRequest itemRequest : request.getItems()) {
            ProductVariant variant = productVariantRepository.findById(itemRequest.getVariantId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.VARIANT_NOT_FOUND));

            Product product = productRepository.findById(variant.getProductId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.VARIANT_NOT_FOUND));

            if (variant.getDeleted() || !"ACTIVE".equals(variant.getStatus()) || product.getDeleted()) {
                throw new BusinessException(ErrorCode.VARIANT_NOT_FOUND);
            }

            BigDecimal subtotal = variant.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem item = new OrderItem();
            item.setVariantId(variant.getId());
            item.setProductName(product.getName());
            item.setVariantSize(variant.getSize());
            item.setUnitPrice(variant.getPrice());
            item.setQuantity(itemRequest.getQuantity());
            item.setSubtotal(subtotal);
            items.add(item);
        }

        List<ProductVariant> touchedVariants = new ArrayList<>();

        for (OrderItemRequest itemRequest : request.getItems()) {
            ProductVariant variant = productVariantRepository.findById(itemRequest.getVariantId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.VARIANT_NOT_FOUND));

            if (variant.getStock() < itemRequest.getQuantity()) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
            }

            variant.setStock(variant.getStock() - itemRequest.getQuantity());
            touchedVariants.add(variant);
        }

        for (ProductVariant variant : touchedVariants) {
            productVariantRepository.save(variant);
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setCustomerName(request.getCustomerName());
        order.setPhone(request.getPhone());
        order.setEmail(request.getEmail());
        order.setShippingMethod(request.getShippingMethod());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setAddress(request.getAddress());
        order.setShippingFee(shippingFee);
        order.setPickupDate(request.getPickupDate());
        order.setRemark(request.getRemark());
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING");
        order.setItems(items);

        order = orderRepository.save(order);

        log.info("訂單已建立，orderNo={}, 品項數量={}", order.getOrderNo(), items.size());

        return toResponse(order);
    }

    /**
     * 產生訂單編號：ORD + 14 碼時間戳（yyyyMMddHHmmss）+ 4 碼亂數，共 21 碼
     * 不依賴資料庫自增 id，可在 Order 物件建立當下就備齊，createOrder() 因此只需一次 save()
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", ThreadLocalRandom.current().nextInt(ORDER_NO_RANDOM_BOUND));
        return "ORD" + timestamp + random;
    }

    /**
     * 寄送訂單成立通知信，失敗只記錄 log，不影響已建立的訂單
     */
    @Override
    public void sendOrderConfirmationEmail(OrderResponse order) {
        try {
            emailApiClient.sendOrderConfirmation(order);
        } catch (Exception e) {
            log.error("訂單通知信寄送失敗，orderNo={}", order.getOrderNo(), e);
        }
    }

    /**
     * 依訂單編號與電話查詢訂單，找不到或電話不符統一回 ORDER_NOT_FOUND
     */
    @Override
    @Transactional(readOnly = true)
    public OrderResponse queryOrder(QueryOrderRequest request) {
        Order order = orderRepository.findByOrderNoAndPhone(request.getOrderNo(), request.getPhone())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        return toResponse(order);
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(item.getProductName(), item.getVariantSize(),
                        item.getQuantity(), item.getUnitPrice(), item.getSubtotal()))
                .toList();

        return new OrderResponse(order.getOrderNo(), order.getStatus(), order.getCustomerName(),
                order.getPhone(), order.getEmail(), order.getShippingMethod(), order.getPaymentMethod(),
                order.getAddress(), order.getPickupDate(), order.getRemark(), itemResponses,
                order.getShippingFee(), order.getTotalAmount(), order.getCreatedAt());
    }
}