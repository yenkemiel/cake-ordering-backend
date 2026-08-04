package com.kemiel.cakeordering.order.service;

import com.kemiel.cakeordering.AbstractIntegrationTest;
import com.kemiel.cakeordering.order.dto.CreateOrderRequest;
import com.kemiel.cakeordering.order.dto.OrderDetailResponse;
import com.kemiel.cakeordering.order.dto.OrderItemRequest;
import com.kemiel.cakeordering.order.dto.OrderResponse;
import com.kemiel.cakeordering.order.dto.UpdateOrderStatusRequest;
import com.kemiel.cakeordering.order.repository.OrderRepository;
import com.kemiel.cakeordering.product.entity.Product;
import com.kemiel.cakeordering.product.entity.ProductVariant;
import com.kemiel.cakeordering.product.repository.ProductRepository;
import com.kemiel.cakeordering.product.repository.ProductVariantRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * 併發鎖序整合測試：重現日誌-11 §1 手動用 curl 重現的死鎖情境，驗證排序修法後
 * 交叉買同一組 variant 的併發訂單不再觸發資料庫死鎖。不使用 Mockito，
 * 死鎖是 InnoDB 鎖排程層級行為，純記憶體模擬測不出來
 */
@Slf4j
class OrderConcurrencyIntegrationTest extends AbstractIntegrationTest {

    private static final int CONCURRENCY_ROUNDS = 20;
    private static final int INITIAL_STOCK = 1000;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

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
     * 比單純先後呼叫更容易踩中鎖排隊的競爭區間。兩個交易鎖定的 variant 完全
     * 重疊（皆為 variantX、variantY），真實併發下其中一方樂觀鎖版本衝突
     * （ObjectOptimisticLockingFailureException）是 @Version 機制本身設計好
     * 的正常結果，既有規格明訂 409 不自動重試；本測試唯一要排除的是死鎖
     * （CannotAcquireLockException 或訊息含 deadlock），兩者不能用同一套
     * 「不准拋例外」的標準衡量，因此分開判斷、分開計數
     */
    @Test
    @DisplayName("兩張訂單 items 順序相反、同時併發送出，重複多輪皆不應觸發資料庫死鎖")
    void shouldNotDeadlock_whenTwoOrdersDeductSameVariantsInReverseOrder() throws InterruptedException, TimeoutException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        int successCountA = 0;
        int successCountB = 0;
        int conflictCount = 0;

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

                if (isOrderSucceeded(futureA)) {
                    successCountA++;
                } else {
                    conflictCount++;
                }

                if (isOrderSucceeded(futureB)) {
                    successCountB++;
                } else {
                    conflictCount++;
                }
            }
        } finally {
            executorService.shutdown();
        }

        int totalSuccessCount = successCountA + successCountB;

        ProductVariant updatedX = productVariantRepository.findById(variantX.getId()).orElseThrow();
        ProductVariant updatedY = productVariantRepository.findById(variantY.getId()).orElseThrow();

        assertThat(updatedX.getStock()).isEqualTo(INITIAL_STOCK - totalSuccessCount);
        assertThat(updatedY.getStock()).isEqualTo(INITIAL_STOCK - totalSuccessCount);

        log.info("併發鎖序測試完成：共 {} 輪，A 方向成功 {} 筆，B 方向成功 {} 筆，總成功 {} 筆，樂觀鎖版本衝突 {} 筆",
                CONCURRENCY_ROUNDS, successCountA, successCountB, totalSuccessCount, conflictCount);
    }

    /**
     * 對單一 future 的成功／失敗做分類：成功時驗證回應內容並回傳 true；
     * 失敗時解開 ExecutionException 拿到真正的例外，交給
     * assertAllowedOptimisticLockConflict() 判斷是否為允許的樂觀鎖衝突，回傳 false
     */
    private boolean isOrderSucceeded(Future<OrderResponse> future) throws InterruptedException, TimeoutException {
        try {
            OrderResponse response = future.get(10, TimeUnit.SECONDS);
            assertThat(response).isNotNull();
            assertThat(response.getOrderNo()).isNotBlank();
            return true;
        } catch (ExecutionException e) {
            assertAllowedOptimisticLockConflict(e.getCause());
            return false;
        }
    }

    /**
     * 併發鎖序整合測試（取消回補庫存路徑）：cancelOrderAndRestock() 先前完全無自動化測試
     * 覆蓋，本測試比照上方建立訂單情境，改為驗證兩張訂單同時取消、回補庫存涉及相同兩個
     * variant 且原始品項順序相反時，不會觸發資料庫死鎖，樂觀鎖版本衝突為允許的正常結果。
     * 每輪先依序（非併發）建立兩張 PENDING 訂單，只有「取消」這個動作真正併發執行——
     * 建立訂單本身的併發鎖序已由上方測試涵蓋，這裡不重複測試
     */
    @Test
    @DisplayName("兩張訂單同時取消、回補庫存涉及相同兩個 variant 且順序相反，重複多輪皆不應觸發資料庫死鎖")
    void shouldNotDeadlock_whenTwoOrdersCancelSameVariantsConcurrently() throws InterruptedException, TimeoutException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        int successCountA = 0;
        int successCountB = 0;
        int conflictCount = 0;

        try {
            for (int round = 0; round < CONCURRENCY_ROUNDS; round++) {
                OrderResponse orderA = orderService.createOrder(buildRequest(variantX.getId(), variantY.getId()));
                OrderResponse orderB = orderService.createOrder(buildRequest(variantY.getId(), variantX.getId()));

                Long orderAId = orderRepository.findByOrderNoAndPhone(orderA.getOrderNo(), orderA.getPhone())
                        .orElseThrow().getId();
                Long orderBId = orderRepository.findByOrderNoAndPhone(orderB.getOrderNo(), orderB.getPhone())
                        .orElseThrow().getId();

                CyclicBarrier barrier = new CyclicBarrier(2);

                Future<OrderDetailResponse> futureA = executorService.submit(() -> {
                    barrier.await();
                    return orderService.updateOrderStatus(orderAId, buildCancelRequest());
                });
                Future<OrderDetailResponse> futureB = executorService.submit(() -> {
                    barrier.await();
                    return orderService.updateOrderStatus(orderBId, buildCancelRequest());
                });

                if (isCancelSucceeded(futureA)) {
                    successCountA++;
                } else {
                    conflictCount++;
                }

                if (isCancelSucceeded(futureB)) {
                    successCountB++;
                } else {
                    conflictCount++;
                }
            }
        } finally {
            executorService.shutdown();
        }

        int totalSuccessCount = successCountA + successCountB;
        int totalCreatedDeduction = CONCURRENCY_ROUNDS * 2;

        ProductVariant updatedX = productVariantRepository.findById(variantX.getId()).orElseThrow();
        ProductVariant updatedY = productVariantRepository.findById(variantY.getId()).orElseThrow();

        assertThat(updatedX.getStock()).isEqualTo(INITIAL_STOCK - totalCreatedDeduction + totalSuccessCount);
        assertThat(updatedY.getStock()).isEqualTo(INITIAL_STOCK - totalCreatedDeduction + totalSuccessCount);

        log.info("併發取消測試完成：共 {} 輪，A 方向取消成功 {} 筆，B 方向取消成功 {} 筆，總成功 {} 筆，樂觀鎖版本衝突 {} 筆",
                CONCURRENCY_ROUNDS, successCountA, successCountB, totalSuccessCount, conflictCount);
    }

    /**
     * 對取消訂單的 future 做成功／失敗分類：成功時驗證訂單狀態確實變為 CANCELLED 並回傳
     * true；失敗時解開 ExecutionException，交給 assertAllowedOptimisticLockConflict()
     * 判斷是否為允許的樂觀鎖衝突，回傳 false
     */
    private boolean isCancelSucceeded(Future<OrderDetailResponse> future) throws InterruptedException, TimeoutException {
        try {
            OrderDetailResponse response = future.get(10, TimeUnit.SECONDS);
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo("CANCELLED");
            return true;
        } catch (ExecutionException e) {
            assertAllowedOptimisticLockConflict(e.getCause());
            return false;
        }
    }

    private UpdateOrderStatusRequest buildCancelRequest() {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setStatus("CANCELLED");
        return request;
    }

    /**
     * 兩個交易寫入集合完全重疊時，樂觀鎖版本衝突（ObjectOptimisticLockingFailureException／
     * OptimisticLockException，沿用 GlobalExceptionHandler.handleOptimisticLock() 既有攔截
     * 的同一對型別）是允許發生的正常結果；死鎖（CannotAcquireLockException 或訊息含
     * deadlock）則是本測試要驗證已排除的回歸，絕對不允許發生。後兩條斷言邏輯上已被第一條
     * 正向斷言隱含涵蓋（兩種例外繼承鏈互斥），刻意保留獨立斷言，讓「死鎖絕對不允許」在
     * 程式碼裡有專屬、講得很白的一行。建立訂單、取消訂單兩支測試共用此判斷邏輯。
     */
    private void assertAllowedOptimisticLockConflict(Throwable cause) {
        assertThat(cause).isInstanceOfAny(ObjectOptimisticLockingFailureException.class, OptimisticLockException.class);
        assertThat(cause).isNotInstanceOf(CannotAcquireLockException.class);
        assertThat(cause.getMessage()).doesNotContainIgnoringCase("deadlock");
    }

    /**
     * 組出兩品項、品項順序可指定的建立訂單請求。建立訂單、取消訂單兩支測試皆需要
     * 用相反順序的 variantId 組請求來重現交叉搶鎖情境，共用此方法。
     */
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