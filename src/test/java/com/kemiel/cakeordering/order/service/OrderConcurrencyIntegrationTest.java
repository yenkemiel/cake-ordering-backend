package com.kemiel.cakeordering.order.service;

import com.kemiel.cakeordering.AbstractIntegrationTest;
import com.kemiel.cakeordering.order.dto.CreateOrderRequest;
import com.kemiel.cakeordering.order.dto.OrderItemRequest;
import com.kemiel.cakeordering.order.dto.OrderResponse;
import com.kemiel.cakeordering.product.entity.Product;
import com.kemiel.cakeordering.product.entity.ProductVariant;
import com.kemiel.cakeordering.product.repository.ProductRepository;
import com.kemiel.cakeordering.product.repository.ProductVariantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

/**
 * 併發鎖序整合測試：重現日誌-11 §1 手動用 curl 重現的死鎖情境，驗證排序修法後
 * 交叉買同一組 variant 的併發訂單不再觸發資料庫死鎖。不使用 Mockito，
 * 死鎖是 InnoDB 鎖排程層級行為，純記憶體模擬測不出來
 */
class OrderConcurrencyIntegrationTest extends AbstractIntegrationTest {

    private static final int CONCURRENCY_ROUNDS = 20;
    private static final int INITIAL_STOCK = 1000;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    private ProductVariant variantX;
    private ProductVariant variantY;

    @BeforeEach
    void setUp() {
        Product product = new Product("併發測試蛋糕", 1L, "OrderConcurrencyIntegrationTest 測試專用，不依賴 seed-test-data.sql", null);
        product = productRepository.save(product);

        variantX = productVariantRepository.save(
                new ProductVariant(product.getId(), "6吋", BigDecimal.valueOf(680), INITIAL_STOCK, "ACTIVE"));
        variantY = productVariantRepository.save(
                new ProductVariant(product.getId(), "8吋", BigDecimal.valueOf(880), INITIAL_STOCK, "ACTIVE"));
    }

    /**
     * 每輪用 CyclicBarrier(2) 讓兩個執行緒卡在同一個起跑點、同時釋放後才呼叫
     * createOrder()，盡量逼近手動 curl 重現時「真正同時發出」的時間窗，
     * 比單純先後呼叫更容易踩中鎖排隊的競爭區間
     */
    @Test
    @DisplayName("兩張訂單 items 順序相反、同時併發送出，重複多輪皆不應觸發資料庫死鎖")
    void shouldNotDeadlock_whenTwoOrdersDeductSameVariantsInReverseOrder() {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
            for (int round = 0; round < CONCURRENCY_ROUNDS; round++) {
                CyclicBarrier barrier = new CyclicBarrier(2);

                Future<OrderResponse> futureA = executorService.submit(() -> {
                    barrier.await();
                    return orderService.createOrder(buildRequest(variantX.getId(), variantY.getId()));
                });
                Future<OrderResponse> futureB = executorService.submit(() -> {
                    barrier.await();
                    return orderService.createOrder(buildRequest(variantY.getId(), variantX.getId()));
                });

                AtomicReference<OrderResponse> responseA = new AtomicReference<>();
                AtomicReference<OrderResponse> responseB = new AtomicReference<>();

                assertThatCode(() -> responseA.set(futureA.get(10, TimeUnit.SECONDS))).doesNotThrowAnyException();
                assertThatCode(() -> responseB.set(futureB.get(10, TimeUnit.SECONDS))).doesNotThrowAnyException();

                assertThat(responseA.get()).isNotNull();
                assertThat(responseA.get().getOrderNo()).isNotBlank();
                assertThat(responseB.get()).isNotNull();
                assertThat(responseB.get().getOrderNo()).isNotBlank();
            }
        } finally {
            executorService.shutdown();
        }

        ProductVariant updatedX = productVariantRepository.findById(variantX.getId()).orElseThrow();
        ProductVariant updatedY = productVariantRepository.findById(variantY.getId()).orElseThrow();

        int expectedDeductionPerVariant = CONCURRENCY_ROUNDS * 2;
        assertThat(updatedX.getStock()).isEqualTo(INITIAL_STOCK - expectedDeductionPerVariant);
        assertThat(updatedY.getStock()).isEqualTo(INITIAL_STOCK - expectedDeductionPerVariant);
    }

    private CreateOrderRequest buildRequest(Long firstVariantId, Long secondVariantId) {
        OrderItemRequest firstItem = new OrderItemRequest();
        firstItem.setVariantId(firstVariantId);
        firstItem.setQuantity(1);

        OrderItemRequest secondItem = new OrderItemRequest();
        secondItem.setVariantId(secondVariantId);
        secondItem.setQuantity(1);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerName("併發測試訪客");
        request.setPhone("0912345678");
        request.setEmail("test@example.com");
        request.setShippingMethod("PICKUP");
        request.setPaymentMethod("STORE_PAYMENT");
        request.setPickupDate(LocalDate.now().plusDays(3));
        request.setItems(List.of(firstItem, secondItem));
        return request;
    }
}
