package com.softwareuniverse.service;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import com.softwareuniverse.dto.request.ProductRequest;
import com.softwareuniverse.dto.response.ProductResponse;
import com.softwareuniverse.dto.response.ProductVariantResponse;
import com.softwareuniverse.entity.*;
import com.softwareuniverse.repository.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminProductServiceImpl implements AdminProductService {

  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final ProductVariantRepository variantRepository;
  private final ProductImageRepository imageRepository;

  @Override
  @Transactional(readOnly = true)
  public Page<ProductResponse> getAllProducts(int page, int size, String status, Long categoryId) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

    Page<Product> products;
    if (categoryId != null) {
      products = productRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable);
    } else {
      products = productRepository.findAll(pageable);
    }

    // Optional filter by status (active/inactive)
    if (status != null && !status.isBlank()) {
      boolean active = "active".equalsIgnoreCase(status);
      List<Product> filtered =
          products.getContent().stream()
              .filter(p -> Boolean.valueOf(active).equals(p.getIsActive()))
              .toList();
      return new PageImpl<>(filtered.stream().map(this::toResponse).toList(), pageable, filtered.size());
    }

    return products.map(this::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public ProductResponse getProduct(Long id) {
    Product p =
        productRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    return toResponse(p);
  }

  @Override
  @Transactional
  public ProductResponse createProduct(ProductRequest request) {
    Category category =
        categoryRepository
            .findById(request.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

    String slug = (request.getSlug() != null && !request.getSlug().isBlank())
        ? slugify(request.getSlug())
        : slugify(request.getTitle());

    if (productRepository.existsBySlug(slug)) {
      throw new RuntimeException("Product with this slug already exists: " + slug);
    }

    Product p = new Product();
    p.setCategory(category);
    applyRequest(p, request);
    p.setSlug(slug);
    productRepository.save(p);

    // Save images
    saveImages(p, request.getImages());

    // Save variants
    saveVariants(p, request.getVariants());

    return toResponse(p);
  }

  @Override
  @Transactional
  public ProductResponse updateProduct(Long id, ProductRequest request) {
    Product p =
        productRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

    if (request.getCategoryId() != null) {
      Category category =
          categoryRepository
              .findById(request.getCategoryId())
              .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
      p.setCategory(category);
    }

    if (request.getSlug() != null && !request.getSlug().isBlank()) {
      String newSlug = slugify(request.getSlug());
      if (!newSlug.equals(p.getSlug()) && productRepository.existsBySlug(newSlug)) {
        throw new RuntimeException("Slug already in use");
      }
      p.setSlug(newSlug);
    }

    applyRequest(p, request);
    productRepository.save(p);

    if (request.getImages() != null) {
      imageRepository.deleteAll(imageRepository.findByProductIdOrderByDisplayOrderAsc(p.getId()));
      saveImages(p, request.getImages());
    }

    if (request.getVariants() != null) {
      variantRepository.deleteAll(
          variantRepository.findByProductIdAndIsActiveTrueOrderByDisplayOrderAsc(p.getId()));
      saveVariants(p, request.getVariants());
    }

    return toResponse(p);
  }

  @Override
  @Transactional
  public void deleteProduct(Long id) {
    Product p =
        productRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    productRepository.delete(p);
  }

  @Override
  @Transactional
  public ProductResponse toggleActive(Long id) {
    Product p =
        productRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    p.setIsActive(!Boolean.TRUE.equals(p.getIsActive()));
    productRepository.save(p);
    return toResponse(p);
  }

  // ============ Helpers ============

  private void applyRequest(Product p, ProductRequest r) {
    if (r.getTitle() != null) p.setTitle(r.getTitle());
    if (r.getDescription() != null) p.setDescription(r.getDescription());
    if (r.getShortDescription() != null) p.setShortDescription(r.getShortDescription());
    if (r.getSeoKeywords() != null) p.setSeoKeywords(r.getSeoKeywords());
    if (r.getMrp() != null) p.setMrp(r.getMrp());
    if (r.getPrice() != null) p.setPrice(r.getPrice());
    if (r.getThumbnailUrl() != null) p.setThumbnailUrl(r.getThumbnailUrl());
    if (r.getDownloadFilePath() != null) p.setDownloadFilePath(r.getDownloadFilePath());
    if (r.getLicenseType() != null) p.setLicenseType(r.getLicenseType());
    if (r.getActivationType() != null) p.setActivationType(r.getActivationType());
    if (r.getHasVariants() != null) p.setHasVariants(r.getHasVariants());
    if (r.getStockQuantity() != null) p.setStockQuantity(r.getStockQuantity());
    if (r.getIsFeatured() != null) p.setIsFeatured(r.getIsFeatured());
    if (r.getIsActive() != null) p.setIsActive(r.getIsActive());
    if (r.getDisplayOrder() != null) p.setDisplayOrder(r.getDisplayOrder());
  }

  private void saveImages(Product p, List<String> images) {
    if (images == null) return;
    int order = 1;
    for (String url : images) {
      if (url == null || url.isBlank()) continue;
      ProductImage img = new ProductImage();
      img.setProduct(p);
      img.setImageUrl(url);
      img.setDisplayOrder(order++);
      imageRepository.save(img);
    }
  }

  private void saveVariants(Product p, List<ProductRequest.ProductVariantRequest> variants) {
    if (variants == null || variants.isEmpty()) return;
    int order = 1;
    for (ProductRequest.ProductVariantRequest v : variants) {
      if (v.getVariantName() == null || v.getVariantName().isBlank()) continue;
      if (v.getPrice() == null) continue;

      ProductVariant pv = new ProductVariant();
      pv.setProduct(p);
      pv.setVariantName(v.getVariantName());
      pv.setMrp(v.getMrp());
      pv.setPrice(v.getPrice());
      pv.setStockQuantity(v.getStockQuantity() != null ? v.getStockQuantity() : 0);
      pv.setDisplayOrder(v.getDisplayOrder() != null ? v.getDisplayOrder() : order);
      pv.setIsActive(v.getIsActive() != null ? v.getIsActive() : true);
      variantRepository.save(pv);
      order++;
    }
  }

  private String slugify(String s) {
    return s.toLowerCase().trim().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
  }

  private ProductResponse toResponse(Product p) {
    List<String> images =
        imageRepository.findByProductIdOrderByDisplayOrderAsc(p.getId()).stream()
            .map(ProductImage::getImageUrl)
            .toList();

    List<ProductVariantResponse> variants =
        Boolean.TRUE.equals(p.getHasVariants())
            ? variantRepository
                .findByProductIdAndIsActiveTrueOrderByDisplayOrderAsc(p.getId())
                .stream()
                .map(
                    v ->
                        ProductVariantResponse.builder()
                            .id(v.getId())
                            .variantName(v.getVariantName())
                            .mrp(v.getMrp())
                            .price(v.getPrice())
                            .stockQuantity(v.getStockQuantity())
                            .isActive(v.getIsActive())
                            .build())
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
        .images(images)
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

  private Integer calculateDiscount(BigDecimal mrp, BigDecimal price) {
    if (mrp == null || price == null || mrp.compareTo(BigDecimal.ZERO) == 0) return 0;
    BigDecimal diff = mrp.subtract(price);
    return diff.multiply(BigDecimal.valueOf(100))
        .divide(mrp, 0, RoundingMode.HALF_UP)
        .intValue();
  }
}