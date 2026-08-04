package com.kemiel.cakeordering.category.service;

import com.kemiel.cakeordering.category.entity.Category;
import com.kemiel.cakeordering.category.repository.CategoryRepository;
import com.kemiel.cakeordering.common.exception.BusinessException;
import com.kemiel.cakeordering.common.exception.ErrorCode;
import com.kemiel.cakeordering.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * 分類 Service 單元測試，涵蓋 deleteCategory() 被商品引用時拒絕刪除、正常刪除、分類不存在三種情境
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    @DisplayName("分類被商品引用時刪除應拋出 CATEGORY_IN_USE，且不呼叫 delete()")
    void shouldThrowCategoryInUse_whenCategoryIsReferencedByProducts() {
        Category category = new Category("水果系列蛋糕", 0);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.countByCategoryId(1L)).thenReturn(3L);

        BusinessException exception = catchThrowableOfType(
                () -> categoryService.deleteCategory(1L), BusinessException.class);
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CATEGORY_IN_USE);

        verify(categoryRepository, never()).delete(any());
    }

    @Test
    @DisplayName("分類未被任何商品引用時應成功刪除")
    void shouldDeleteCategory_whenNotReferencedByAnyProduct() {
        Category category = new Category("測試用可刪分類", 5);

        when(categoryRepository.findById(6L)).thenReturn(Optional.of(category));
        when(productRepository.countByCategoryId(6L)).thenReturn(0L);

        categoryService.deleteCategory(6L);

        verify(categoryRepository).delete(category);
        verify(categoryRepository).flush();
    }

    @Test
    @DisplayName("分類不存在時應拋出 CATEGORY_NOT_FOUND")
    void shouldThrowCategoryNotFound_whenCategoryDoesNotExist() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException exception = catchThrowableOfType(
                () -> categoryService.deleteCategory(999L), BusinessException.class);
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);

        verify(productRepository, never()).countByCategoryId(any());
    }
}