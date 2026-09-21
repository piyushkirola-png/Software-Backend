package com.softwareuniverse.service;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import com.softwareuniverse.dto.response.CategoryResponse;
import com.softwareuniverse.entity.Category;
import com.softwareuniverse.repository.CategoryRepository;
import com.softwareuniverse.repository.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

  private final CategoryRepository categoryRepository;
  private final ProductRepository productRepository;

  @Override
  @Transactional(readOnly = true)
  public List<CategoryResponse> getAllActiveCategories() {
    return categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
        .map(this::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public CategoryResponse getCategoryBySlug(String slug) {
    Category category =
        categoryRepository
            .findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + slug));
    return toResponse(category);
  }

  private CategoryResponse toResponse(Category c) {
    Long productCount = c.getProducts() != null ? (long) c.getProducts().size() : 0L;
    return CategoryResponse.builder()
        .id(c.getId())
        .name(c.getName())
        .slug(c.getSlug())
        .description(c.getDescription())
        .imageUrl(c.getImageUrl())
        .iconUrl(c.getIconUrl())
        .displayOrder(c.getDisplayOrder())
        .isActive(c.getIsActive())
        .productCount(productCount)
        .build();
  }
}
