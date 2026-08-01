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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * variant／product 第一次呼叫 findById 變成 Hibernate managed 狀態的順序，決定了
     * flush 時 UPDATE product_variants 實際送出的順序，因此存在性檢查與庫存檢查／扣庫存
     * 都依 variantId 排序後的 sortedItems 進行；金額計算與組 OrderItem 改成查已載入好的
     * Map（不重新呼叫 findById），維持訂單明細顯示順序與 client 原始送出順序一致
     * variantMap／productMap 的 key 統一用 itemRequest.getVariantId()（request 帶入值），
     * 不用 variant.getId()（entity 自身欄位）——真實 DB 情境下兩者恆等，但不應讓 Map 查找
     * 依賴「entity 載入後自報的 id 是否等於查詢時用的 id」這個隱性假設，見開發日誌-11 §1.1
     */
    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        BigDecimal shippingFee = "DELIVERY".equals(request.getShippingMethod())
                ? BigDecimal.valueOf(250)
                : BigDecimal.ZERO;

        List<OrderItemRequest> sortedItems = request.getItems().stream()
                .sorted(Comparator.comparing(OrderItemRequest::getVariantId))
                .toList();

        Map<Long, ProductVariant> variantMap = new HashMap<>();
        Map<Long, Product> productMap = new HashMap<>();

        for (OrderItemRequest itemRequest : sortedItems) {
            ProductVariant variant = productVariantRepository.findById(itemRequest.getVariantId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.VARIANT_NOT_FOUND));

            Product product = productRepository.findById(variant.getProductId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.VARIANT_NOT_FOUND));

            if (variant.getDeleted() || !"ACTIVE".equals(variant.getStatus()) || product.getDeleted()) {
                throw new BusinessException(ErrorCode.VARIANT_NOT_FOUND);
            }

            variantMap.put(itemRequest.getVariantId(), variant);
            productMap.put(itemRequest.getVariantId(), product);
        }

        List<OrderItem> items = new ArrayList<>();
        BigDecimal totalAmount = shippingFee;

        for (OrderItemRequest itemRequest : request.getItems()) {
            ProductVariant variant = variantMap.get(itemRequest.getVariantId());
            Product product = productMap.get(itemRequest.getVariantId());

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

        for (OrderItemRequest itemRequest : sortedItems) {
            ProductVariant variant = variantMap.get(itemRequest.getVariantId());

            if (variant.getStock() < itemRequest.getQuantity()) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
            }

            variant.setStock(variant.getStock() - itemRequest.getQuantity());
        }

        productVariantRepository.saveAll(variantMap.values());
        productVariantRepository.flush();

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

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", ThreadLocalRandom.current().nextInt(ORDER_NO_RANDOM_BOUND));
        return "ORD" + timestamp + random;
    }

    @Override
    public void sendOrderConfirmationEmail(OrderResponse order) {
        try {
            emailApiClient.sendOrderConfirmation(order);
        } catch (Exception e) {
            log.error("訂單通知信寄送失敗，orderNo={}", order.getOrderNo(), e);
        }
    }

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