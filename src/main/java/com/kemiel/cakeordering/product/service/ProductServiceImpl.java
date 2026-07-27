package com.kemiel.cakeordering.product.service;

import com.kemiel.cakeordering.category.entity.Category;
import com.kemiel.cakeordering.category.repository.CategoryRepository;
import com.kemiel.cakeordering.common.exception.BusinessException;
import com.kemiel.cakeordering.common.exception.ErrorCode;
import com.kemiel.cakeordering.common.response.PageResult;
import com.kemiel.cakeordering.product.dto.CreateProductRequest;
import com.kemiel.cakeordering.product.dto.ProductCustomerDetailResponse;
import com.kemiel.cakeordering.product.dto.ProductCustomerSummaryResponse;
import com.kemiel.cakeordering.product.dto.ProductResponse;
import com.kemiel.cakeordering.product.dto.ProductSummaryResponse;
import com.kemiel.cakeordering.product.dto.ProductVariantCustomerResponse;
import com.kemiel.cakeordering.product.dto.ProductVariantRequest;
import com.kemiel.cakeordering.product.dto.ProductVariantSummaryResponse;
import com.kemiel.cakeordering.product.dto.UpdateProductRequest;
import com.kemiel.cakeordering.product.entity.Product;
import com.kemiel.cakeordering.product.entity.ProductVariant;
import com.kemiel.cakeordering.product.entity.VariantStatus;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 商品 Service 實作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 後台商品列表查詢，可依 categoryId 篩選、依 name 排序，今日暫不支援 status 篩選（記入技術債）
     */
    @Override
    public PageResult<ProductSummaryResponse> listProductsForAdmin(List<Long> categoryIds, String sort,
                                                                   Integer page, Integer size) {
        boolean hasCategoryFilter = categoryIds != null && !categoryIds.isEmpty();
        if (hasCategoryFilter) {
            validateCategoryIds(categoryIds);
        }
        Pageable pageable = buildPageable(page, size, sort);
        Page<Product> productPage = hasCategoryFilter
                ? productRepository.findByIsDeletedFalseAndCategoryIdIn(categoryIds, pageable)
                : productRepository.findByIsDeletedFalse(pageable);

        List<Product> products = productPage.getContent();
        Map<Long, String> categoryNames = loadCategoryNames(products);
        Map<Long, List<ProductVariant>> variantsByProduct = loadVariantsByProduct(products);

        List<ProductSummaryResponse> content = new ArrayList<>();
        for (Product product : products) {
            List<ProductVariant> variants = variantsByProduct.get(product.getId());
            if (variants == null) {
                variants = new ArrayList<>();
            }
            content.add(toSummaryResponse(product, categoryNames.get(product.getCategoryId()), variants));
        }
        return new PageResult<>(content, pageable.getPageNumber(), pageable.getPageSize(),
                productPage.getTotalElements());
    }

    /**
     * 前台商品列表查詢，僅回傳底下至少一個上架中變體的商品
     */
    @Override
    public PageResult<ProductCustomerSummaryResponse> listProductsForCustomer(Long categoryId, Integer page,
                                                                              Integer size) {
        if (categoryId != null && !categoryRepository.existsById(categoryId)) {
            throw new BusinessException(ErrorCode.CATEGORY_INVALID);
        }
        Pageable pageable = buildCustomerPageable(page, size);
        Page<Product> productPage = categoryId != null
                ? productRepository.findActiveByCategoryId(categoryId, pageable)
                : productRepository.findActive(pageable);

        List<Product> products = productPage.getContent();
        Map<Long, String> categoryNames = loadCategoryNames(products);

        List<Long> productIds = new ArrayList<>();
        for (Product product : products) {
            productIds.add(product.getId());
        }

        Map<Long, List<ProductVariant>> variantsByProduct = new HashMap<>();
        if (!productIds.isEmpty()) {
            List<ProductVariant> activeVariants =
                    productVariantRepository.findByProductIdInAndActiveAndIsDeletedFalse(productIds);
            for (ProductVariant variant : activeVariants) {
                List<ProductVariant> list = variantsByProduct.get(variant.getProductId());
                if (list == null) {
                    list = new ArrayList<>();
                    variantsByProduct.put(variant.getProductId(), list);
                }
                list.add(variant);
            }
        }

        List<ProductCustomerSummaryResponse> content = new ArrayList<>();
        for (Product product : products) {
            List<ProductVariant> variants = variantsByProduct.get(product.getId());
            if (variants == null) {
                variants = new ArrayList<>();
            }
            content.add(toCustomerSummaryResponse(product, categoryNames.get(product.getCategoryId()), variants));
        }
        return new PageResult<>(content, pageable.getPageNumber(), pageable.getPageSize(),
                productPage.getTotalElements());
    }

    /**
     * 前台商品詳細查詢，商品不存在、已刪除或無上架變體皆統一回 PRODUCT_NOT_FOUND
     */
    @Override
    public ProductCustomerDetailResponse getProductDetail(Long id) {
        Product product = productRepository.findActiveById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        String categoryName = categoryRepository.findById(product.getCategoryId())
                .map(Category::getName)
                .orElse(null);
        List<ProductVariant> variants = productVariantRepository
                .findByProductIdAndActiveAndIsDeletedFalse(product.getId());
        return toCustomerDetailResponse(product, categoryName, variants);
    }

    /**
     * 新增商品，商品與至少一筆變體在同一交易內一併建立
     */
    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        if (!categoryRepository.existsById(request.getCategoryId())) {
            throw new BusinessException(ErrorCode.CATEGORY_INVALID);
        }
        Product product = new Product(request.getName(), request.getCategoryId(),
                request.getDescription(), request.getImageUrl());
        Product savedProduct = productRepository.save(product);

        List<ProductVariant> variants = new ArrayList<>();
        for (ProductVariantRequest variantRequest : request.getVariants()) {
            String status = variantRequest.getStatus() != null
                    ? variantRequest.getStatus()
                    : VariantStatus.ACTIVE.name();
            variants.add(new ProductVariant(savedProduct.getId(), variantRequest.getSize(),
                    variantRequest.getPrice(), variantRequest.getStock(), status));
        }
        List<ProductVariant> savedVariants = productVariantRepository.saveAll(variants);

        log.info("商品已建立，id={}, 變體數量={}", savedProduct.getId(), savedVariants.size());
        String categoryName = categoryRepository.findById(savedProduct.getCategoryId())
                .map(Category::getName)
                .orElse(null);
        return toResponse(savedProduct, categoryName, savedVariants);
    }

    /**
     * 編輯商品基本資料，不影響底下變體
     */
    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = productRepository.findByIsDeletedFalseAndId(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        if (!categoryRepository.existsById(request.getCategoryId())) {
            throw new BusinessException(ErrorCode.CATEGORY_INVALID);
        }
        product.setName(request.getName());
        product.setCategoryId(request.getCategoryId());
        product.setDescription(request.getDescription());
        product.setImageUrl(request.getImageUrl());
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);

        log.info("商品已更新，id={}", product.getId());
        String categoryName = categoryRepository.findById(product.getCategoryId())
                .map(Category::getName)
                .orElse(null);
        List<ProductVariant> variants = productVariantRepository.findByProductIdAndIsDeletedFalse(product.getId());
        return toResponse(product, categoryName, variants);
    }

    /**
     * 刪除商品（軟刪除），連動軟刪除底下所有變體
     */
    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findByIsDeletedFalseAndId(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        product.setDeleted(true);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);

        List<ProductVariant> variants = productVariantRepository.findByProductIdAndIsDeletedFalse(product.getId());
        for (ProductVariant variant : variants) {
            variant.setDeleted(true);
            variant.setUpdatedAt(LocalDateTime.now());
        }
        productVariantRepository.saveAll(variants);

        log.info("商品已刪除（含底下變體），id={}, 變體數量={}", product.getId(), variants.size());
    }

    /**
     * 驗證傳入的 categoryId 清單皆存在於 categories 表，不存在任一筆即拋出 CATEGORY_INVALID
     */
    private void validateCategoryIds(List<Long> categoryIds) {
        List<Category> found = categoryRepository.findAllById(categoryIds);
        Set<Long> distinctIds = new HashSet<>(categoryIds);
        if (found.size() != distinctIds.size()) {
            throw new BusinessException(ErrorCode.CATEGORY_INVALID);
        }
    }

    /**
     * 組出後台列表用的 Pageable，page／size 不合法（page &lt; 0 或 size &lt; 1）回 VALIDATION_ERROR，
     * sort 僅接受 name,asc 或 name,desc，其餘格式同樣回 VALIDATION_ERROR
     */
    private Pageable buildPageable(Integer page, Integer size, String sort) {
        int p = page != null ? page : 0;
        int s = size != null ? size : 20;
        if (p < 0 || s < 1) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        if (sort == null || sort.isBlank()) {
            return PageRequest.of(p, s, Sort.by(Sort.Order.asc("id")));
        }
        String[] parts = sort.split(",");
        if (parts.length != 2 || !"name".equals(parts[0])
                || !(parts[1].equals("asc") || parts[1].equals("desc"))) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        Sort.Direction direction = Sort.Direction.fromString(parts[1]);
        return PageRequest.of(p, s, Sort.by(new Sort.Order(direction, "name")));
    }

    /**
     * 組出前台列表用的 Pageable，page／size 不合法（page &lt; 0 或 size &lt; 1）回 VALIDATION_ERROR
     */
    private Pageable buildCustomerPageable(Integer page, Integer size) {
        int p = page != null ? page : 0;
        int s = size != null ? size : 20;
        if (p < 0 || s < 1) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        return PageRequest.of(p, s, Sort.by(Sort.Order.asc("id")));
    }

    /**
     * 批次查出商品清單對應的分類名稱，避免逐筆查詢造成 N+1
     */
    private Map<Long, String> loadCategoryNames(List<Product> products) {
        Set<Long> categoryIds = new HashSet<>();
        for (Product product : products) {
            categoryIds.add(product.getCategoryId());
        }
        Map<Long, String> result = new HashMap<>();
        if (categoryIds.isEmpty()) {
            return result;
        }
        for (Category category : categoryRepository.findAllById(categoryIds)) {
            result.put(category.getId(), category.getName());
        }
        return result;
    }

    /**
     * 批次查出商品清單底下未刪除的變體，依 productId 分組，避免逐筆查詢造成 N+1
     */
    private Map<Long, List<ProductVariant>> loadVariantsByProduct(List<Product> products) {
        List<Long> productIds = new ArrayList<>();
        for (Product product : products) {
            productIds.add(product.getId());
        }
        Map<Long, List<ProductVariant>> result = new HashMap<>();
        if (productIds.isEmpty()) {
            return result;
        }
        for (ProductVariant variant : productVariantRepository.findByProductIdInAndIsDeletedFalse(productIds)) {
            List<ProductVariant> list = result.get(variant.getProductId());
            if (list == null) {
                list = new ArrayList<>();
                result.put(variant.getProductId(), list);
            }
            list.add(variant);
        }
        return result;
    }

    /**
     * 將 Product 轉換為後台列表用 ProductSummaryResponse
     */
    private ProductSummaryResponse toSummaryResponse(Product product, String categoryName,
                                                     List<ProductVariant> variants) {
        List<ProductVariantSummaryResponse> variantResponses = new ArrayList<>();
        for (ProductVariant variant : variants) {
            variantResponses.add(new ProductVariantSummaryResponse(variant.getId(), variant.getSize(),
                    variant.getPrice(), variant.getStock(), variant.getStatus()));
        }
        return new ProductSummaryResponse(product.getId(), product.getName(), product.getCategoryId(), categoryName,
                product.getDescription(), product.getImageUrl(), variantResponses, product.getUpdatedAt());
    }

    /**
     * 將 Product 轉換為後台新增／編輯回應用 ProductResponse
     */
    private ProductResponse toResponse(Product product, String categoryName, List<ProductVariant> variants) {
        List<ProductVariantSummaryResponse> variantResponses = new ArrayList<>();
        for (ProductVariant variant : variants) {
            variantResponses.add(new ProductVariantSummaryResponse(variant.getId(), variant.getSize(),
                    variant.getPrice(), variant.getStock(), variant.getStatus()));
        }
        return new ProductResponse(product.getId(), product.getName(), product.getCategoryId(), categoryName,
                product.getDescription(), product.getImageUrl(), variantResponses,
                product.getCreatedAt(), product.getUpdatedAt());
    }

    /**
     * 將 Product 轉換為前台列表用 ProductCustomerSummaryResponse
     */
    private ProductCustomerSummaryResponse toCustomerSummaryResponse(Product product, String categoryName,
                                                                     List<ProductVariant> variants) {
        List<ProductVariantCustomerResponse> variantResponses = new ArrayList<>();
        for (ProductVariant variant : variants) {
            variantResponses.add(new ProductVariantCustomerResponse(variant.getId(), variant.getSize(),
                    variant.getPrice(), variant.getStock()));
        }
        return new ProductCustomerSummaryResponse(product.getId(), product.getName(), product.getCategoryId(),
                categoryName, product.getImageUrl(), variantResponses);
    }

    /**
     * 將 Product 轉換為前台詳細用 ProductCustomerDetailResponse
     */
    private ProductCustomerDetailResponse toCustomerDetailResponse(Product product, String categoryName,
                                                                   List<ProductVariant> variants) {
        List<ProductVariantCustomerResponse> variantResponses = new ArrayList<>();
        for (ProductVariant variant : variants) {
            variantResponses.add(new ProductVariantCustomerResponse(variant.getId(), variant.getSize(),
                    variant.getPrice(), variant.getStock()));
        }
        return new ProductCustomerDetailResponse(product.getId(), product.getName(), product.getCategoryId(),
                categoryName, product.getDescription(), product.getImageUrl(), variantResponses);
    }
}
