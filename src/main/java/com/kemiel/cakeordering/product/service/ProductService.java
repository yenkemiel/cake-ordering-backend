package com.kemiel.cakeordering.product.service;

import com.kemiel.cakeordering.common.response.PageResult;
import com.kemiel.cakeordering.product.dto.CreateProductRequest;
import com.kemiel.cakeordering.product.dto.ProductCustomerDetailResponse;
import com.kemiel.cakeordering.product.dto.ProductCustomerSummaryResponse;
import com.kemiel.cakeordering.product.dto.ProductResponse;
import com.kemiel.cakeordering.product.dto.ProductSummaryResponse;
import com.kemiel.cakeordering.product.dto.UpdateProductRequest;

import java.util.List;

/**
 * 商品 Service 介面
 */
public interface ProductService {

    PageResult<ProductSummaryResponse> listProductsForAdmin(List<Long> categoryIds, String sort,
                                                            Integer page, Integer size);

    PageResult<ProductCustomerSummaryResponse> listProductsForCustomer(Long categoryId, Integer page, Integer size);

    ProductCustomerDetailResponse getProductDetail(Long id);

    ProductResponse createProduct(CreateProductRequest request);

    ProductResponse updateProduct(Long id, UpdateProductRequest request);

    void deleteProduct(Long id);
}
