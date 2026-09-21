package com.softwareuniverse.service;

import com.softwareuniverse.dto.response.CategoryResponse;
import java.util.List;

public interface CategoryService {

  List<CategoryResponse> getAllActiveCategories();

  CategoryResponse getCategoryBySlug(String slug);
}
