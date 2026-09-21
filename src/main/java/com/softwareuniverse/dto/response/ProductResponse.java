package com.softwareuniverse.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
  private Long id;
  private Long categoryId;
  private String categoryName;
  private String categorySlug;
  private String title;
  private String slug;
  private String description;
  private String shortDescription;
  private String seoKeywords;
  private BigDecimal mrp;
  private BigDecimal price;
  private Integer discountPercent;
  private String thumbnailUrl;
  private List<String> images;
  private String licenseType;
  private String activationType;
  private Boolean hasVariants;
  private Integer stockQuantity;
  private Boolean isFeatured;
  private Boolean isActive;
  private Integer displayOrder;
  private Double ratingAvg;
  private Integer ratingCount;
  private List<ProductVariantResponse> variants;
}
