package com.softwareuniverse.controller;

import com.softwareuniverse.common.response.ApiResponse;
import com.softwareuniverse.dto.response.CategoryResponse;
import com.softwareuniverse.service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

  private final CategoryService categoryService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAll() {
    return ResponseEntity.ok(
        ApiResponse.success("Categories fetched", categoryService.getAllActiveCategories()));
  }

  @GetMapping("/{slug}")
  public ResponseEntity<ApiResponse<CategoryResponse>> getBySlug(@PathVariable String slug) {
    return ResponseEntity.ok(
        ApiResponse.success("Category fetched", categoryService.getCategoryBySlug(slug)));
  }
}
