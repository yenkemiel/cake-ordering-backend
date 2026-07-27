package com.kemiel.cakeordering.product.repository;

import com.kemiel.cakeordering.product.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 商品變體 Repository
 */
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findByProductId(Long productId);

    @Query("SELECT v FROM ProductVariant v WHERE v.productId = :productId AND v.isDeleted = false")
    List<ProductVariant> findByProductIdAndIsDeletedFalse(@Param("productId") Long productId);

    @Query("SELECT v FROM ProductVariant v WHERE v.productId IN :productIds AND v.isDeleted = false")
    List<ProductVariant> findByProductIdInAndIsDeletedFalse(@Param("productIds") List<Long> productIds);

    @Query("SELECT v FROM ProductVariant v WHERE v.productId = :productId "
            + "AND v.status = 'ACTIVE' AND v.isDeleted = false")
    List<ProductVariant> findByProductIdAndActiveAndIsDeletedFalse(@Param("productId") Long productId);

    @Query("SELECT v FROM ProductVariant v WHERE v.productId IN :productIds "
            + "AND v.status = 'ACTIVE' AND v.isDeleted = false")
    List<ProductVariant> findByProductIdInAndActiveAndIsDeletedFalse(@Param("productIds") List<Long> productIds);
}

