package com.kemiel.cakeordering.order.service;

import com.kemiel.cakeordering.AbstractIntegrationTest;
import com.kemiel.cakeordering.category.entity.Category;
import com.kemiel.cakeordering.category.repository.CategoryRepository;
import com.kemiel.cakeordering.order.dto.CreateOrderRequest;
import com.kemiel.cakeordering.order.dto.OrderItemRequest;
import com.kemiel.cakeordering.order.dto.OrderResponse;
import com.kemiel.cakeordering.order.email.EmailApiClient;
import com.kemiel.cakeordering.order.entity.Order;
import com.kemiel.cakeordering.order.repository.OrderRepository;
import com.kemiel.cakeordering.product.entity.Product;
import com.kemiel.cakeordering.product.entity.ProductVariant;
import com.kemiel.cakeordering.product.repository.ProductRepository;
import com.kemiel.cakeordering.product.repository.ProductVariantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

/**
 * order 模組 Testcontainers 整合測試，驗證建立訂單在真實 MySQL 交易行為下的正確性
 */
class OrderServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @MockitoBean
    private EmailApiClient emailApiClient;

    private ProductVariant testVariant;

    @BeforeEach
    void setUp() {
        Category category = categoryRepository.save(new Category("整合測試分類-" + System.nanoTime(), 1));

        Product product = new Product("整合測試蛋糕", category.getId(),
                "Testcontainers 測試專用，不依賴 seed-test-data.sql", null);
        product = productRepository.save(product);

        ProductVariant variant = new ProductVariant(product.getId(), "6吋", BigDecimal.valueOf(680), 5, "ACTIVE");
        testVariant = productVariantRepository.save(variant);
    }

    @Test
    @Transactional
    @DisplayName("庫存足夠時應成功建立訂單並扣減庫存")
    void shouldCreateOrderSuccessfully_whenStockSufficient() {
        CreateOrderRequest request = buildRequest(2);

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.getOrderNo()).isNotBlank();
        assertThat(response.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(1360));

        ProductVariant updated = productVariantRepository.findById(testVariant.getId()).orElseThrow();
        assertThat(updated.getStock()).isEqualTo(3);

        Order savedOrder = orderRepository.findByOrderNoAndPhone(response.getOrderNo(), request.getPhone())
                .orElseThrow();
        assertThat(savedOrder.getItems()).hasSize(1);
    }

    @Test
    @DisplayName("Email 寄送失敗不影響訂單已建立的結果")
    void shouldKeepOrderCreated_whenEmailSendFails() {
        doThrow(new RuntimeException("Resend API timeout"))
                .when(emailApiClient).sendOrderConfirmation(any());

        CreateOrderRequest request = buildRequest(1);
        OrderResponse response = orderService.createOrder(request);

        assertThatCode(() -> orderService.sendOrderConfirmationEmail(response))
                .doesNotThrowAnyException();

        assertThat(orderRepository.findByOrderNoAndPhone(response.getOrderNo(), request.getPhone()))
                .isPresent();
    }

    private CreateOrderRequest buildRequest(int quantity) {
        OrderItemRequest item = new OrderItemRequest();
        item.setVariantId(testVariant.getId());
        item.setQuantity(quantity);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerName("測試訪客");
        request.setPhone("0912345678");
        request.setEmail("test@example.com");
        request.setShippingMethod("PICKUP");
        request.setPaymentMethod("STORE_PAYMENT");
        request.setPickupDate(LocalDate.now().plusDays(3));
        request.setItems(List.of(item));
        return request;
    }
}