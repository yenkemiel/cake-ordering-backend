package com.kemiel.cakeordering.category.service;

import com.kemiel.cakeordering.category.dto.CategoryResponse;
import com.kemiel.cakeordering.category.dto.CategorySummaryResponse;
import com.kemiel.cakeordering.category.dto.CreateCategoryRequest;
import com.kemiel.cakeordering.category.dto.ReorderCategoryRequest;
import com.kemiel.cakeordering.category.dto.UpdateCategoryRequest;
import com.kemiel.cakeordering.category.entity.Category;
import com.kemiel.cakeordering.category.repository.CategoryRepository;
import com.kemiel.cakeordering.common.exception.BusinessException;
import com.kemiel.cakeordering.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分類 Service 實作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 查詢所有分類，依 sort_order 升冪排序
     */
    @Override
    public List<CategorySummaryResponse> listCategories() {
        List<Category> categories = categoryRepository.findAllByOrderBySortOrderAsc();
        List<CategorySummaryResponse> result = new ArrayList<>();
        for (Category c : categories) {
            result.add(new CategorySummaryResponse(c.getId(), c.getName()));
        }
        return result;
    }

    /**
     * 新增分類，名稱重複（trim 後不分大小寫）拋出例外，新分類的 sort_order 固定排在最後
     */
    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        String name = request.getName().trim();
        if (categoryRepository.existsByName(name)) {
            throw new BusinessException(ErrorCode.CATEGORY_NAME_DUPLICATE);
        }
        int sortOrder = (int) categoryRepository.count();
        Category saved;
        try {
            saved = categoryRepository.save(new Category(name, sortOrder));
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.CATEGORY_NAME_DUPLICATE);
        }
        log.info("新增分類成功，categoryId={}, name={}", saved.getId(), saved.getName());
        return toResponse(saved);
    }

    /**
     * 更新分類名稱，重複檢查排除自己
     */
    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
        String name = request.getName().trim();
        if (categoryRepository.existsByNameAndIdNot(name, id)) {
            throw new BusinessException(ErrorCode.CATEGORY_NAME_DUPLICATE);
        }
        category.setName(name);
        category.setUpdatedAt(LocalDateTime.now());
        try {
            categoryRepository.saveAndFlush(category);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.CATEGORY_NAME_DUPLICATE);
        }
        log.info("編輯分類成功，categoryId={}, name={}", category.getId(), category.getName());
        return toResponse(category);
    }

    /**
     * 刪除分類，若仍有商品（含已軟刪除）引用則拋出例外
     */
    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
        Long productCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(id) FROM products WHERE category_id = ?", Long.class, id);
        if (productCount != null && productCount > 0) {
            throw new BusinessException(ErrorCode.CATEGORY_IN_USE);
        }
        try {
            categoryRepository.delete(category);
            categoryRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.CATEGORY_IN_USE);
        }
        log.info("刪除分類成功，categoryId={}", id);
    }

    /**
     * 依傳入的 categoryIds 順序，將每個分類的 sort_order 更新為其在陣列中的索引值（0-based）
     *
     * @param request 排序後的完整分類 id 順序
     */
    @Override
    @Transactional
    public void reorderCategories(ReorderCategoryRequest request) {
        List<Long> categoryIds = request.getCategoryIds();
        if (categoryIds.size() != categoryRepository.count()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        List<Category> found = categoryRepository.findAllById(categoryIds);
        if (found.size() != categoryIds.size()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        Map<Long, Category> categoryMap = new HashMap<>();
        for (Category category : found) {
            categoryMap.put(category.getId(), category);
        }
        for (int i = 0; i < categoryIds.size(); i++) {
            Category category = categoryMap.get(categoryIds.get(i));
            category.setSortOrder(i);
            category.setUpdatedAt(LocalDateTime.now());
        }
        categoryRepository.saveAll(found);
        log.info("調整分類排序成功，categoryIds={}", categoryIds);
    }

    /**
     * 將 Category 轉換為 CategoryResponse
     */
    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getCreatedAt());
    }
}