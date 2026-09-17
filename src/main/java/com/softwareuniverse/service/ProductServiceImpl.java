package com.softwareuniverse.service;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import com.softwareuniverse.dto.response.ProductResponse;
import com.softwareuniverse.dto.response.ProductVariantResponse;
import com.softwareuniverse.entity.Category;
import com.softwareuniverse.entity.Product;
import com.softwareuniverse.entity.ProductImage;
import com.softwareuniverse.entity.ProductVariant;
import com.softwareuniverse.repository.CategoryRepository;
import com.softwareuniverse.repository.ProductImageRepository;
import com.softwareuniverse.repository.ProductRepository;
import com.softwareuniverse.repository.ProductVariantRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final ProductVariantRepository variantRepository;
  private final ProductImageRepository imageRepository;

  @Override
  @Transactional(readOnly = true)
  public List<ProductResponse> getFeaturedProducts() {
    return productRepository.findByIsFeaturedTrueAndIsActiveTrueOrderByDisplayOrderAsc().stream()
        .map(this::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ProductResponse> getAllActiveProducts(int page, int size, String sortBy) {
    Pageable pageable = PageRequest.of(page, size, resolveSort(sortBy));
    return productRepository.findByIsActiveTrue(pageable).map(this::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ProductResponse> getProductsByCategorySlug(
      String slug, int page, int size, String sortBy) {
    Category category =
        categoryRepository
            .findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + slug));
    Pageable pageable = PageRequest.of(page, size, resolveSort(sortBy));
    return productRepository
        .findByCategoryIdAndIsActiveTrue(category.getId(), pageable)
        .map(this::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ProductResponse> searchProducts(String query, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("displayOrder").ascending());
    return productRepository.searchActive(query, pageable).map(this::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public ProductResponse getProductBySlug(String slug) {
    Product product =
        productRepository
            .findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + slug));
    return toResponse(product);
  }

  private Sort resolveSort(String sortBy) {
    if (sortBy == null) return Sort.by("displayOrder").ascending();
    return switch (sortBy.toLowerCase()) {
      case "price_asc" -> Sort.by("price").ascending();
      case "price_desc" -> Sort.by("price").descending();
      case "newest" -> Sort.by("createdAt").descending();
      case "rating" -> Sort.by("ratingAvg").descending();
      default -> Sort.by("displayOrder").ascending();
    };
  }

  private ProductResponse toResponse(Product p) {
    List<String> imageUrls =
        imageRepository.findByProductIdOrderByDisplayOrderAsc(p.getId()).stream()
            .map(ProductImage::getImageUrl)
            .toList();

    List<ProductVariantResponse> variants =
        Boolean.TRUE.equals(p.getHasVariants())
            ? variantRepository
                .findByProductIdAndIsActiveTrueOrderByDisplayOrderAsc(p.getId())
                .stream()
                .map(this::toVariantResponse)
                .toList()
            : List.of();

    return ProductResponse.builder()
        .id(p.getId())
        .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
        .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
        .categorySlug(p.getCategory() != null ? p.getCategory().getSlug() : null)
        .title(p.getTitle())
        .slug(p.getSlug())
        .description(p.getDescription())
        .shortDescription(p.getShortDescription())
        .seoKeywords(p.getSeoKeywords())
        .mrp(p.getMrp())
        .price(p.getPrice())
        .discountPercent(calculateDiscount(p.getMrp(), p.getPrice()))
        .thumbnailUrl(p.getThumbnailUrl())
        .images(imageUrls)
        .licenseType(p.getLicenseType())
        .activationType(p.getActivationType())
        .hasVariants(p.getHasVariants())
        .stockQuantity(p.getStockQuantity())
        .isFeatured(p.getIsFeatured())
        .isActive(p.getIsActive())
        .ratingAvg(p.getRatingAvg())
        .ratingCount(p.getRatingCount())
        .variants(variants)
        .build();
  }

  private ProductVariantResponse toVariantResponse(ProductVariant v) {
    return ProductVariantResponse.builder()
        .id(v.getId())
        .variantName(v.getVariantName())
        .mrp(v.getMrp())
        .price(v.getPrice())
        .stockQuantity(v.getStockQuantity())
        .isActive(v.getIsActive())
        .build();
  }

  private Integer calculateDiscount(BigDecimal mrp, BigDecimal price) {
    if (mrp == null || price == null || mrp.compareTo(BigDecimal.ZERO) == 0) return 0;
    BigDecimal diff = mrp.subtract(price);
    return diff.multiply(BigDecimal.valueOf(100))
        .divide(mrp, 0, RoundingMode.HALF_UP)
        .intValue();
  }
}