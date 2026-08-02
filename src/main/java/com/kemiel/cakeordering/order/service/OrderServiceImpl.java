package com.kemiel.cakeordering.order.service;

import com.kemiel.cakeordering.common.exception.BusinessException;
import com.kemiel.cakeordering.common.exception.ErrorCode;
import com.kemiel.cakeordering.common.response.PageResult;
import com.kemiel.cakeordering.order.dto.CreateOrderRequest;
import com.kemiel.cakeordering.order.dto.OrderDetailResponse;
import com.kemiel.cakeordering.order.dto.OrderItemRequest;
import com.kemiel.cakeordering.order.dto.OrderItemResponse;
import com.kemiel.cakeordering.order.dto.OrderResponse;
import com.kemiel.cakeordering.order.dto.OrderSummaryResponse;
import com.kemiel.cakeordering.order.dto.QueryOrderRequest;
import com.kemiel.cakeordering.order.dto.UpdateOrderStatusRequest;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_SHIPPED = "SHIPPED";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_CANCELLED = "CANCELLED";

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

    /**
     * 產生訂單編號，格式 ORD + 時間戳 + 四位隨機數
     */
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

    /**
     * 依訂單編號與電話查詢單筆訂單，找不到時統一回 ORDER_NOT_FOUND，
     * 不區分是編號錯還是電話錯，避免透露訂單是否存在
     */
    @Override
    @Transactional(readOnly = true)
    public OrderResponse queryOrder(QueryOrderRequest request) {
        Order order = orderRepository.findByOrderNoAndPhone(request.getOrderNo(), request.getPhone())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        return toResponse(order);
    }

    /**
     * 將 Order 轉換為訪客端完整回應格式，供建立訂單與查詢訂單共用
     */
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

    /**
     * 查詢後台訂單列表，可依 status 篩選，未帶 status 時查詢全部訂單，依建立時間降冪排序
     */
    @Override
    @Transactional(readOnly = true)
    public PageResult<OrderSummaryResponse> listOrdersForAdmin(String status, Integer page, Integer size) {
        if (status != null && !isValidStatus(status)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        Pageable pageable = buildPageable(page, size);
        Page<Order> orderPage = (status != null)
                ? orderRepository.findByStatus(status, pageable)
                : orderRepository.findAll(pageable);

        List<OrderSummaryResponse> content = new ArrayList<>();
        for (Order order : orderPage.getContent()) {
            content.add(toSummaryResponse(order));
        }
        return new PageResult<>(content, orderPage.getNumber(), orderPage.getSize(), orderPage.getTotalElements());
    }

    /**
     * 檢查 status 是否為 PENDING／SHIPPED／COMPLETED／CANCELLED 其中之一
     */
    private boolean isValidStatus(String status) {
        return STATUS_PENDING.equals(status) || STATUS_SHIPPED.equals(status)
                || STATUS_COMPLETED.equals(status) || STATUS_CANCELLED.equals(status);
    }

    /**
     * page／size 為 null 時採用預設值（page=0, size=20），依 createdAt 降冪排序，
     * 讓後台人員優先看到新進訂單
     */
    private Pageable buildPageable(Integer page, Integer size) {
        int resolvedPage = (page != null) ? page : 0;
        int resolvedSize = (size != null) ? size : 20;
        return PageRequest.of(resolvedPage, resolvedSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    /**
     * 將 Order 轉換為列表用的精簡回應格式
     */
    private OrderSummaryResponse toSummaryResponse(Order order) {
        return new OrderSummaryResponse(order.getId(), order.getOrderNo(), order.getCustomerName(),
                order.getStatus(), order.getPickupDate(), order.getTotalAmount(), order.getCreatedAt());
    }

    /**
     * 查詢單筆訂單完整明細，供後台檢視使用
     */
    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetailForAdmin(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_ORDER_NOT_FOUND));
        return toDetailResponse(order);
    }

    /**
     * 依狀態機規則更新訂單狀態；取消時同步回補品項對應變體庫存
     */
    @Override
    @Transactional
    public OrderDetailResponse updateOrderStatus(Long id, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_ORDER_NOT_FOUND));
        String currentStatus = order.getStatus();
        String targetStatus = request.getStatus();

        if (STATUS_CANCELLED.equals(targetStatus)) {
            if (!STATUS_PENDING.equals(currentStatus)) {
                throw new BusinessException(ErrorCode.ORDER_CANCEL_NOT_ALLOWED);
            }
            cancelOrderAndRestock(order);
        } else if (STATUS_SHIPPED.equals(targetStatus)) {
            if (!STATUS_PENDING.equals(currentStatus)) {
                throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS_TRANSITION);
            }
            order.setStatus(STATUS_SHIPPED);
            orderRepository.saveAndFlush(order);
        } else {
            if (!STATUS_SHIPPED.equals(currentStatus)) {
                throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS_TRANSITION);
            }
            order.setStatus(STATUS_COMPLETED);
            orderRepository.saveAndFlush(order);
        }

        log.info("訂單狀態更新，orderId={}, orderNo={}, from={}, to={}",
                order.getId(), order.getOrderNo(), currentStatus, order.getStatus());
        return toDetailResponse(order);
    }

    /**
     * 取消訂單並回補品項對應變體庫存，比照建立訂單扣庫存的三段式鎖序結構
     * ：先依 variantId 排序載入並檢查存在性，再依訂單原始品項順序
     * 累加回補量於記憶體中，最後統一對受影響的變體呼叫 save()，避免多品項
     * 交叉搶鎖情境下的死鎖風險
     */
    private void cancelOrderAndRestock(Order order) {
        List<OrderItem> items = new ArrayList<>(order.getItems());
        items.sort(Comparator.comparing(OrderItem::getVariantId));

        Map<Long, ProductVariant> variantMap = new LinkedHashMap<>();
        for (OrderItem item : items) {
            if (!variantMap.containsKey(item.getVariantId())) {
                ProductVariant variant = productVariantRepository.findById(item.getVariantId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.VARIANT_NOT_FOUND));
                variantMap.put(item.getVariantId(), variant);
            }
        }

        for (OrderItem item : order.getItems()) {
            ProductVariant variant = variantMap.get(item.getVariantId());
            variant.setStock(variant.getStock() + item.getQuantity());
        }

        for (ProductVariant variant : variantMap.values()) {
            productVariantRepository.saveAndFlush(variant);
        }

        order.setStatus(STATUS_CANCELLED);
        orderRepository.saveAndFlush(order);
    }

    /**
     * 將 Order 轉換為後台明細用的完整回應格式（含 id）
     */
    private OrderDetailResponse toDetailResponse(Order order) {
        List<OrderItemResponse> itemResponses = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            itemResponses.add(new OrderItemResponse(item.getProductName(), item.getVariantSize(),
                    item.getQuantity(), item.getUnitPrice(), item.getSubtotal()));
        }
        return new OrderDetailResponse(order.getId(), order.getOrderNo(), order.getStatus(),
                order.getCustomerName(), order.getPhone(), order.getEmail(), order.getShippingMethod(),
                order.getPaymentMethod(), order.getAddress(), order.getPickupDate(), order.getRemark(),
                itemResponses, order.getShippingFee(), order.getTotalAmount(), order.getCreatedAt());
    }
}