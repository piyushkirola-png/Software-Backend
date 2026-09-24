package com.softwareuniverse.service;

import com.softwareuniverse.dto.request.ProductRequest;
import com.softwareuniverse.dto.response.ProductResponse;
import org.springframework.data.domain.Page;

public interface AdminProductService {
  Page<ProductResponse> getAllProducts(
    int page,
    int size,
    String name,
    Long categoryId,
    String status,
    String licenseType,
    java.math.BigDecimal minPrice,
    java.math.BigDecimal maxPrice,
    String sortBy
  );

  ProductResponse getProduct(Long id);

  ProductResponse createProduct(ProductRequest request);

  ProductResponse updateProduct(Long id, ProductRequest request);

  void deleteProduct(Long id);

  ProductResponse toggleActive(Long id);
}
