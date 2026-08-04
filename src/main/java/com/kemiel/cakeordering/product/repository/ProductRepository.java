package com.kemiel.cakeordering.product.repository;

import com.kemiel.cakeordering.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 商品 Repository
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.isDeleted = false "
            + "AND (:status IS NULL OR EXISTS (SELECT 1 FROM ProductVariant v WHERE v.productId = p.id "
            + "AND v.isDeleted = false AND v.status = :status))")
    Page<Product> findForAdmin(@Param("status") String status, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isDeleted = false AND p.categoryId IN :categoryIds "
            + "AND (:status IS NULL OR EXISTS (SELECT 1 FROM ProductVariant v WHERE v.productId = p.id "
            + "AND v.isDeleted = false AND v.status = :status))")
    Page<Product> findForAdminByCategoryIds(@Param("categoryIds") List<Long> categoryIds,
            @Param("status") String status, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isDeleted = false AND p.id = :id")
    Product findByIsDeletedFalseAndId(@Param("id") Long id);

    @Query("SELECT p FROM Product p WHERE p.isDeleted = false "
            + "AND EXISTS (SELECT 1 FROM ProductVariant v WHERE v.productId = p.id "
            + "AND v.status = 'ACTIVE' AND v.isDeleted = false)")
    Page<Product> findActive(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isDeleted = false AND p.categoryId = :categoryId "
            + "AND EXISTS (SELECT 1 FROM ProductVariant v WHERE v.productId = p.id "
            + "AND v.status = 'ACTIVE' AND v.isDeleted = false)")
    Page<Product> findActiveByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isDeleted = false AND p.categoryId <> :excludeCategoryId "
            + "AND EXISTS (SELECT 1 FROM ProductVariant v WHERE v.productId = p.id "
            + "AND v.status = 'ACTIVE' AND v.isDeleted = false)")
    Page<Product> findActiveExcludingCategory(@Param("excludeCategoryId") Long excludeCategoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isDeleted = false AND p.categoryId = :categoryId "
            + "AND p.categoryId <> :excludeCategoryId "
            + "AND EXISTS (SELECT 1 FROM ProductVariant v WHERE v.productId = p.id "
            + "AND v.status = 'ACTIVE' AND v.isDeleted = false)")
    Page<Product> findActiveByCategoryIdExcluding(@Param("categoryId") Long categoryId,
            @Param("excludeCategoryId") Long excludeCategoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isDeleted = false AND p.id = :id "
            + "AND EXISTS (SELECT 1 FROM ProductVariant v WHERE v.productId = p.id "
            + "AND v.status = 'ACTIVE' AND v.isDeleted = false)")
    Product findActiveById(@Param("id") Long id);

    long countByCategoryId(Long categoryId);
}