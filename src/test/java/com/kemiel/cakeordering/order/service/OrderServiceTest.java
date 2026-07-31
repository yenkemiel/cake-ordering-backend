package com.kemiel.cakeordering.order.service;

import com.kemiel.cakeordering.common.exception.BusinessException;
import com.kemiel.cakeordering.common.exception.ErrorCode;
import com.kemiel.cakeordering.order.dto.CreateOrderRequest;
import com.kemiel.cakeordering.order.dto.OrderItemRequest;
import com.kemiel.cakeordering.order.email.EmailApiClient;
import com.kemiel.cakeordering.order.repository.OrderRepository;
import com.kemiel.cakeordering.product.entity.Product;
import com.kemiel.cakeordering.product.entity.ProductVariant;
import com.kemiel.cakeordering.product.repository.ProductRepository;
import com.kemiel.cakeordering.product.repository.ProductVariantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * OrderServiceImpl 純邏輯單元測試，不啟動 Spring Context，Repository 全數 mock
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private EmailApiClient emailApiClient;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    @DisplayName("庫存不足時應拋出 INSUFFICIENT_STOCK，且不呼叫任何 save()")
    void shouldThrowInsufficientStock_whenStockNotEnough() {
        ProductVariant variant = new ProductVariant(100L, "6吋", BigDecimal.valueOf(680), 1, "ACTIVE");
        variant.setDeleted(false);
        Product product = new Product("測試蛋糕", 1L, "測試用", null);
        product.setDeleted(false);

        when(productVariantRepository.findById(1L)).thenReturn(Optional.of(variant));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        CreateOrderRequest request = buildRequest(5);

        BusinessException exception = catchThrowableOfType(
                () -> orderService.createOrder(request), BusinessException.class);
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INSUFFICIENT_STOCK);

        verify(productVariantRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    private CreateOrderRequest buildRequest(int quantity) {
        OrderItemRequest item = new OrderItemRequest();
        item.setVariantId(1L);
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