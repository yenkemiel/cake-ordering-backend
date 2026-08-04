package com.kemiel.cakeordering.product.service;

import com.kemiel.cakeordering.category.repository.CategoryRepository;
import com.kemiel.cakeordering.common.exception.BusinessException;
import com.kemiel.cakeordering.common.exception.ErrorCode;
import com.kemiel.cakeordering.product.dto.ProductVariantResponse;
import com.kemiel.cakeordering.product.dto.UpdateVariantStatusRequest;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 商品 Service 單元測試，涵蓋 deleteProduct() 級聯軟刪除、deleteVariant() 最後一個變體保護、
 * updateVariantStatus() 上下架切換三項核心業務規則
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    @DisplayName("刪除商品應同時軟刪除底下所有未刪除變體")
    void shouldSoftDeleteProductAndCascadeVariants() {
        Product product = new Product("水果蛋糕", 1L, "測試用", null);
        product.setDeleted(false);
        ReflectionTestUtils.setField(product, "id", 10L);

        ProductVariant variantA = new ProductVariant(10L, "6吋", BigDecimal.valueOf(680), 5, "ACTIVE");
        variantA.setDeleted(false);
        ProductVariant variantB = new ProductVariant(10L, "8吋", BigDecimal.valueOf(980), 3, "INACTIVE");
        variantB.setDeleted(false);

        when(productRepository.findByIsDeletedFalseAndId(10L)).thenReturn(product);
        when(productVariantRepository.findByProductIdAndIsDeletedFalse(10L))
                .thenReturn(List.of(variantA, variantB));

        productService.deleteProduct(10L);

        assertThat(product.getDeleted()).isTrue();
        assertThat(variantA.getDeleted()).isTrue();
        assertThat(variantB.getDeleted()).isTrue();

        verify(productRepository).save(product);
        verify(productVariantRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("刪除不存在或已刪除的商品應拋出 PRODUCT_NOT_FOUND")
    void shouldThrowProductNotFound_whenDeletingNonExistentProduct() {
        when(productRepository.findByIsDeletedFalseAndId(999L)).thenReturn(null);

        BusinessException exception = catchThrowableOfType(
                () -> productService.deleteProduct(999L), BusinessException.class);
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);

        verify(productVariantRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("僅剩最後一個未刪除變體時，刪除變體應拋出 VARIANT_DELETE_NOT_ALLOWED，且不呼叫 save()")
    void shouldThrowVariantDeleteNotAllowed_whenOnlyOneVariantRemains() {
        Product product = new Product("檸檬塔", 1L, "測試用", null);
        product.setDeleted(false);
        ProductVariant variant = new ProductVariant(20L, "6吋", BigDecimal.valueOf(580), 4, "ACTIVE");
        variant.setDeleted(false);

        when(productRepository.findByIsDeletedFalseAndId(20L)).thenReturn(product);
        when(productVariantRepository.findByIdAndProductIdAndIsDeletedFalse(200L, 20L)).thenReturn(variant);
        when(productVariantRepository.countByProductIdAndIsDeletedFalse(20L)).thenReturn(1L);

        BusinessException exception = catchThrowableOfType(
                () -> productService.deleteVariant(20L, 200L), BusinessException.class);
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.VARIANT_DELETE_NOT_ALLOWED);

        verify(productVariantRepository, never()).save(any());
    }

    @Test
    @DisplayName("尚有其他未刪除變體時，刪除變體應正常成功")
    void shouldDeleteVariant_whenOtherVariantsRemain() {
        Product product = new Product("檸檬塔", 1L, "測試用", null);
        product.setDeleted(false);
        ProductVariant variant = new ProductVariant(20L, "8吋", BigDecimal.valueOf(880), 2, "ACTIVE");
        variant.setDeleted(false);

        when(productRepository.findByIsDeletedFalseAndId(20L)).thenReturn(product);
        when(productVariantRepository.findByIdAndProductIdAndIsDeletedFalse(201L, 20L)).thenReturn(variant);
        when(productVariantRepository.countByProductIdAndIsDeletedFalse(20L)).thenReturn(2L);

        productService.deleteVariant(20L, 201L);

        assertThat(variant.getDeleted()).isTrue();
        verify(productVariantRepository).save(variant);
    }

    @Test
    @DisplayName("切換變體上下架只影響該變體本身的 status")
    void shouldToggleVariantStatus_withoutAffectingOtherFields() {
        Product product = new Product("原味巴斯克", 1L, "測試用", null);
        product.setDeleted(false);
        ProductVariant variant = new ProductVariant(30L, "6吋", BigDecimal.valueOf(750), 6, "ACTIVE");
        variant.setDeleted(false);

        when(productRepository.findByIsDeletedFalseAndId(30L)).thenReturn(product);
        when(productVariantRepository.findByIdAndProductIdAndIsDeletedFalse(300L, 30L)).thenReturn(variant);

        UpdateVariantStatusRequest request = new UpdateVariantStatusRequest();
        request.setStatus("INACTIVE");

        ProductVariantResponse response = productService.updateVariantStatus(30L, 300L, request);

        assertThat(response.getStatus()).isEqualTo("INACTIVE");
        assertThat(response.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(750));
        assertThat(response.getStock()).isEqualTo(6);
        verify(productVariantRepository).save(variant);
    }
}