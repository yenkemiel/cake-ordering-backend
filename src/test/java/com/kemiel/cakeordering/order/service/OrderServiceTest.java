package com.kemiel.cakeordering.order.service;

import com.kemiel.cakeordering.common.exception.BusinessException;
import com.kemiel.cakeordering.common.exception.ErrorCode;
import com.kemiel.cakeordering.order.dto.CreateOrderRequest;
import com.kemiel.cakeordering.order.dto.OrderItemRequest;
import com.kemiel.cakeordering.order.dto.OrderResponse;
import com.kemiel.cakeordering.order.dto.UpdateOrderStatusRequest;
import com.kemiel.cakeordering.order.email.EmailApiClient;
import com.kemiel.cakeordering.order.entity.Order;
import com.kemiel.cakeordering.order.entity.OrderItem;
import com.kemiel.cakeordering.order.repository.OrderRepository;
import com.kemiel.cakeordering.product.entity.Product;
import com.kemiel.cakeordering.product.entity.ProductVariant;
import com.kemiel.cakeordering.product.repository.ProductRepository;
import com.kemiel.cakeordering.product.repository.ProductVariantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
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

    @Test
    @DisplayName("庫存足夠時應成功建立訂單，正確扣庫存並回傳 PENDING 訂單")
    void shouldCreateOrder_whenStockIsSufficient() {
        ProductVariant variant = new ProductVariant(100L, "6吋", BigDecimal.valueOf(680), 10, "ACTIVE");
        variant.setDeleted(false);
        Product product = new Product("測試蛋糕", 1L, "測試用", null);
        product.setDeleted(false);

        when(productVariantRepository.findById(1L)).thenReturn(Optional.of(variant));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateOrderRequest request = buildRequest(2);

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(1360));
        assertThat(response.getItems()).hasSize(1);
        assertThat(variant.getStock()).isEqualTo(8);

        verify(productVariantRepository).saveAll(anyCollection());
        verify(productVariantRepository).flush();

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getStatus()).isEqualTo("PENDING");
        assertThat(orderCaptor.getValue().getOrderNo()).startsWith("ORD");
    }

    @Test
    @DisplayName("扣庫存時發生樂觀鎖版本衝突，應向上拋出不吞掉，且不建立訂單")
    void shouldPropagateOptimisticLockException_whenVersionConflict() {
        ProductVariant variant = new ProductVariant(100L, "6吋", BigDecimal.valueOf(680), 10, "ACTIVE");
        variant.setDeleted(false);
        Product product = new Product("測試蛋糕", 1L, "測試用", null);
        product.setDeleted(false);

        when(productVariantRepository.findById(1L)).thenReturn(Optional.of(variant));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(productVariantRepository.saveAll(anyCollection()))
                .thenThrow(new ObjectOptimisticLockingFailureException(ProductVariant.class, 1L));
        
        CreateOrderRequest request = buildRequest(2);

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("取消訂單應回補所有品項庫存，並將狀態更新為 CANCELLED")
    void shouldRestockAndCancelOrder_whenCancellingPendingOrder() {
        ProductVariant variantX = new ProductVariant(100L, "6吋", BigDecimal.valueOf(680), 3, "ACTIVE");
        ProductVariant variantY = new ProductVariant(101L, "8吋", BigDecimal.valueOf(980), 2, "ACTIVE");

        OrderItem itemX = new OrderItem();
        itemX.setVariantId(1L);
        itemX.setQuantity(2);
        OrderItem itemY = new OrderItem();
        itemY.setVariantId(2L);
        itemY.setQuantity(1);

        Order order = new Order();
        order.setOrderNo("ORD202608040000001111");
        order.setStatus("PENDING");
        order.setItems(List.of(itemX, itemY));

        when(orderRepository.findById(9L)).thenReturn(Optional.of(order));
        when(productVariantRepository.findById(1L)).thenReturn(Optional.of(variantX));
        when(productVariantRepository.findById(2L)).thenReturn(Optional.of(variantY));

        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setStatus("CANCELLED");

        orderService.updateOrderStatus(9L, request);

        assertThat(variantX.getStock()).isEqualTo(5);
        assertThat(variantY.getStock()).isEqualTo(3);
        assertThat(order.getStatus()).isEqualTo("CANCELLED");

        verify(productVariantRepository).saveAll(anyCollection());
        verify(productVariantRepository).flush();
        verify(orderRepository).saveAndFlush(order);
    }

    @Test
    @DisplayName("generateOrderNo() 格式須為 ORD + 14 碼時間戳 + 4 碼亂數，共 21 碼")
    void generateOrderNo_shouldMatchExpectedFormat() {
        String orderNo = orderService.generateOrderNo();

        assertThat(orderNo).hasSize(21);
        assertThat(orderNo).startsWith("ORD");
        assertThat(orderNo.substring(3)).matches("\\d{18}");
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