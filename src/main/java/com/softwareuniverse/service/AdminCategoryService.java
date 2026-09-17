package com.softwareuniverse.service;

import com.softwareuniverse.dto.request.CategoryRequest;
import com.softwareuniverse.dto.response.CategoryResponse;
import java.util.List;

public interface AdminCategoryService {

  List<CategoryResponse> getAllCategories();

  CategoryResponse getCategory(Long id);

  CategoryResponse createCategory(CategoryRequest request);

  CategoryResponse updateCategory(Long id, CategoryRequest request);

  void deleteCategory(Long id);
}