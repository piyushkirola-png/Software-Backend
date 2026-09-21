package com.softwareuniverse.dto.request;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class ProductRequest {
  private Long categoryId;
  private String title;
  private String slug;
  private String description;
  private String shortDescription;
  private String seoKeywords;
  private BigDecimal mrp;
  private BigDecimal price;
  private String thumbnailUrl;
  private String downloadFilePath;
  private String licenseType;
  private String activationType;
  private Boolean hasVariants;
  private Integer stockQuantity;
  private Boolean isFeatured;
  private Boolean isActive;
  private Integer displayOrder;
  private List<String> images;
  private List<ProductVariantRequest> variants;

  @Data
  public static class ProductVariantRequest {
    private String variantName;
    private BigDecimal mrp;
    private BigDecimal price;
    private Integer stockQuantity;
    private Integer displayOrder;
    private Boolean isActive;
  }
}
