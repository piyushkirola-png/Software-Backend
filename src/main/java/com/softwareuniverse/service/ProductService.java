package com.softwareuniverse.service;

import com.softwareuniverse.dto.response.ProductResponse;
import java.util.List;
import org.springframework.data.domain.Page;

public interface ProductService {

  List<ProductResponse> getFeaturedProducts();

  Page<ProductResponse> getAllActiveProducts(int page, int size, String sortBy);

  Page<ProductResponse> getProductsByCategorySlug(
      String slug, int page, int size, String sortBy);

  Page<ProductResponse> searchProducts(String query, int page, int size);

  ProductResponse getProductBySlug(String slug);
}