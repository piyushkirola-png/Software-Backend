package com.softwareuniverse.controller;

import com.softwareuniverse.common.response.ApiResponse;
import com.softwareuniverse.dto.request.CategoryRequest;
import com.softwareuniverse.dto.response.CategoryResponse;
import com.softwareuniverse.service.AdminCategoryService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {

  private final AdminCategoryService adminCategoryService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAll() {
    return ResponseEntity.ok(
      ApiResponse.success(
        "Categories fetched",
        adminCategoryService.getAllCategories()
      )
    );
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<CategoryResponse>> get(
    @PathVariable Long id
  ) {
    return ResponseEntity.ok(
      ApiResponse.success(
        "Category fetched",
        adminCategoryService.getCategory(id)
      )
    );
  }

  @PostMapping
  public ResponseEntity<ApiResponse<CategoryResponse>> create(
    @Valid @RequestBody CategoryRequest request
  ) {
    return ResponseEntity.ok(
      ApiResponse.success(
        "Category created",
        adminCategoryService.createCategory(request)
      )
    );
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<CategoryResponse>> update(
    @PathVariable Long id,
    @Valid @RequestBody CategoryRequest request
  ) {
    return ResponseEntity.ok(
      ApiResponse.success(
        "Category updated",
        adminCategoryService.updateCategory(id, request)
      )
    );
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
    adminCategoryService.deleteCategory(id);
    return ResponseEntity.ok(ApiResponse.success("Category deleted", null));
  }
}
