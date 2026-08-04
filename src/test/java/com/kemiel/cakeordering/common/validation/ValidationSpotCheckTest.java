package com.kemiel.cakeordering.common.validation;

import com.kemiel.cakeordering.order.dto.CreateOrderRequest;
import com.kemiel.cakeordering.order.dto.OrderItemRequest;
import com.kemiel.cakeordering.product.dto.CreateProductRequest;
import com.kemiel.cakeordering.product.dto.ProductVariantRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DTO 驗證抽查測試，直接以 Jakarta Bean Validation Validator 驗證 CreateOrderRequest 與
 * CreateProductRequest 的跨欄位規則與集合驗證，不透過 Controller 或 MockMvc
 */
class ValidationSpotCheckTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("shippingMethod 為 DELIVERY 但 address 為空時應驗證失敗")
    void shouldFailValidation_whenDeliveryWithoutAddress() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerName("測試訪客");
        request.setPhone("0912345678");
        request.setEmail("test@example.com");
        request.setShippingMethod("DELIVERY");
        request.setPaymentMethod("ONLINE_PAYMENT");
        request.setAddress(null);
        request.setPickupDate(LocalDate.now().plusDays(3));
        OrderItemRequest item = new OrderItemRequest();
        item.setVariantId(1L);
        item.setQuantity(1);
        request.setItems(List.of(item));

        Set<ConstraintViolation<CreateOrderRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("宅配時地址為必填");
    }

    @Test
    @DisplayName("新增商品時 variants 為空陣列應驗證失敗")
    void shouldFailValidation_whenVariantsIsEmpty() {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("測試蛋糕");
        request.setCategoryId(1L);
        request.setVariants(List.of());

        Set<ConstraintViolation<CreateProductRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("至少須帶一筆變體");
    }

    @Test
    @DisplayName("新增商品時任一變體 price 為 0 應驗證失敗")
    void shouldFailValidation_whenVariantPriceIsZero() {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("測試蛋糕");
        request.setCategoryId(1L);
        ProductVariantRequest variant = new ProductVariantRequest();
        variant.setSize("6吋");
        variant.setPrice(BigDecimal.ZERO);
        variant.setStock(5);
        request.setVariants(List.of(variant));

        Set<ConstraintViolation<CreateProductRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("價格須大於 0");
    }
}