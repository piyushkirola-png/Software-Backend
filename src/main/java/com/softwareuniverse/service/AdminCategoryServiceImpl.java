package com.softwareuniverse.service;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import com.softwareuniverse.dto.request.CategoryRequest;
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
public class AdminCategoryServiceImpl implements AdminCategoryService {

  private final CategoryRepository categoryRepository;
  private final ProductRepository productRepository;

  @Override
  @Transactional(readOnly = true)
  public List<CategoryResponse> getAllCategories() {
    return categoryRepository.findAll().stream()
        .sorted(
            (a, b) -> {
              int ao = a.getDisplayOrder() != null ? a.getDisplayOrder() : 0;
              int bo = b.getDisplayOrder() != null ? b.getDisplayOrder() : 0;
              return Integer.compare(ao, bo);
            })
        .map(this::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public CategoryResponse getCategory(Long id) {
    Category c =
        categoryRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    return toResponse(c);
  }

  @Override
  @Transactional
  public CategoryResponse createCategory(CategoryRequest request) {
    String slug =
        (request.getSlug() != null && !request.getSlug().isBlank())
            ? slugify(request.getSlug())
            : slugify(request.getName());

    if (categoryRepository.existsBySlug(slug)) {
      throw new RuntimeException("Category with this slug already exists: " + slug);
    }
    if (categoryRepository.existsByName(request.getName())) {
      throw new RuntimeException("Category with this name already exists");
    }

    Category c = new Category();
    c.setName(request.getName());
    c.setSlug(slug);
    c.setDescription(request.getDescription());
    c.setImageUrl(request.getImageUrl());
    c.setIconUrl(request.getIconUrl());
    c.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
    c.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

    categoryRepository.save(c);
    return toResponse(c);
  }

  @Override
  @Transactional
  public CategoryResponse updateCategory(Long id, CategoryRequest request) {
    Category c =
        categoryRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

    if (request.getName() != null && !request.getName().isBlank()) {
      c.setName(request.getName());
    }

    if (request.getSlug() != null && !request.getSlug().isBlank()) {
      String newSlug = slugify(request.getSlug());
      if (!newSlug.equals(c.getSlug()) && categoryRepository.existsBySlug(newSlug)) {
        throw new RuntimeException("Slug already in use: " + newSlug);
      }
      c.setSlug(newSlug);
    }

    if (request.getDescription() != null) c.setDescription(request.getDescription());
    if (request.getImageUrl() != null) c.setImageUrl(request.getImageUrl());
    if (request.getIconUrl() != null) c.setIconUrl(request.getIconUrl());
    if (request.getDisplayOrder() != null) c.setDisplayOrder(request.getDisplayOrder());
    if (request.getIsActive() != null) c.setIsActive(request.getIsActive());

    categoryRepository.save(c);
    return toResponse(c);
  }

  @Override
  @Transactional
  public void deleteCategory(Long id) {
    Category c =
        categoryRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

    long productCount =
        productRepository.findByCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(id).size();
    if (productCount > 0) {
      throw new RuntimeException(
          "Cannot delete category with " + productCount + " products. Deactivate it instead.");
    }
    categoryRepository.delete(c);
  }

  // ============ Helpers ============

  private String slugify(String s) {
    return s.toLowerCase().trim().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
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
