package com.softwareuniverse.service;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import com.softwareuniverse.dto.response.CategoryResponse;
import com.softwareuniverse.entity.Category;
import com.softwareuniverse.repository.CategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

  private final CategoryRepository categoryRepository;

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
    return toResponse(resolveCategoryEntity(slug));
  }

  /** Resolve a Category by slug with graceful fallbacks. */
  @Override
  @Transactional(readOnly = true)
  public Category resolveCategoryEntity(String slug) {
    if (slug == null || slug.isBlank()) {
      throw new ResourceNotFoundException("Category not found: " + slug);
    }

    String trimmed = slug.trim();

    // 1. Exact
    var exact = categoryRepository.findBySlug(trimmed);
    if (exact.isPresent()) return exact.get();

    // 2. Case-insensitive
    var ci = categoryRepository.findBySlugIgnoreCase(trimmed);
    if (ci.isPresent()) return ci.get();

    // 3. Substring — shortest slug wins
    List<Category> partials =
        categoryRepository.findBySlugContainingIgnoreCaseOrderBySlugAsc(trimmed);
    if (!partials.isEmpty()) {
      return partials.stream()
          .min((a, b) -> Integer.compare(a.getSlug().length(), b.getSlug().length()))
          .orElse(partials.get(0));
    }

    throw new ResourceNotFoundException("Category not found: " + slug);
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
