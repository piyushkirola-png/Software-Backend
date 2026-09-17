package com.softwareuniverse.controller;

import com.softwareuniverse.common.response.ApiResponse;
import com.softwareuniverse.dto.request.ProductRequest;
import com.softwareuniverse.dto.response.ProductResponse;
import com.softwareuniverse.service.AdminProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

  private final AdminProductService adminProductService;

  @GetMapping
  public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAll(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Long categoryId) {
    return ResponseEntity.ok(
        ApiResponse.success(
            "Products fetched",
            adminProductService.getAllProducts(page, size, status, categoryId)));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<ProductResponse>> get(@PathVariable Long id) {
    return ResponseEntity.ok(
        ApiResponse.success("Product fetched", adminProductService.getProduct(id)));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<ProductResponse>> create(
      @Valid @RequestBody ProductRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success("Product created", adminProductService.createProduct(request)));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<ProductResponse>> update(
      @PathVariable Long id, @Valid @RequestBody ProductRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success("Product updated", adminProductService.updateProduct(id, request)));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
    adminProductService.deleteProduct(id);
    return ResponseEntity.ok(ApiResponse.success("Product deleted", null));
  }

  @PostMapping("/{id}/toggle-active")
  public ResponseEntity<ApiResponse<ProductResponse>> toggleActive(@PathVariable Long id) {
    return ResponseEntity.ok(
        ApiResponse.success("Product toggled", adminProductService.toggleActive(id)));
  }
}