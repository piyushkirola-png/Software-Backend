package com.softwareuniverse.controller;

import com.softwareuniverse.common.response.ApiResponse;
import com.softwareuniverse.dto.response.ProductResponse;
import com.softwareuniverse.service.ProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  @GetMapping("/featured")
  public ResponseEntity<ApiResponse<List<ProductResponse>>> getFeatured() {
    return ResponseEntity.ok(
        ApiResponse.success("Featured products fetched", productService.getFeaturedProducts()));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAll(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size,
      @RequestParam(required = false) String sortBy) {
    return ResponseEntity.ok(
        ApiResponse.success("Products fetched", productService.getAllActiveProducts(page, size, sortBy)));
  }

  @GetMapping("/category/{slug}")
  public ResponseEntity<ApiResponse<Page<ProductResponse>>> getByCategory(
      @PathVariable String slug,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size,
      @RequestParam(required = false) String sortBy) {
    return ResponseEntity.ok(
        ApiResponse.success(
            "Products fetched",
            productService.getProductsByCategorySlug(slug, page, size, sortBy)));
  }

  @GetMapping("/search")
  public ResponseEntity<ApiResponse<Page<ProductResponse>>> search(
      @RequestParam String q,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {
    return ResponseEntity.ok(
        ApiResponse.success("Search results", productService.searchProducts(q, page, size)));
  }

  @GetMapping("/{slug}")
  public ResponseEntity<ApiResponse<ProductResponse>> getBySlug(@PathVariable String slug) {
    return ResponseEntity.ok(
        ApiResponse.success("Product fetched", productService.getProductBySlug(slug)));
  }
}