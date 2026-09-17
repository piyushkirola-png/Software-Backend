package com.softwareuniverse.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;

  @Column(nullable = false, length = 255)
  private String title;

  @Column(nullable = false, unique = true, length = 255)
  private String slug;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "short_description", length = 500)
  private String shortDescription;

  @Column(name = "seo_keywords", length = 500)
  private String seoKeywords;

  @Column(name = "mrp", precision = 10, scale = 2)
  private BigDecimal mrp;

  @Column(name = "price", nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @Column(name = "thumbnail_url", length = 500)
  private String thumbnailUrl;

  @Column(name = "download_file_path", length = 500)
  private String downloadFilePath; // e.g. /software/windows/windows11pro.zip

  @Column(name = "license_type", length = 50)
  private String licenseType; // e.g. "Lifetime", "1 Year", "Perpetual"

  @Column(name = "activation_type", length = 50)
  private String activationType; // e.g. "Online", "Phone"

  @Column(name = "has_variants", nullable = false)
  private Boolean hasVariants = false;

  @Column(name = "stock_quantity")
  private Integer stockQuantity = 0;

  @Column(name = "is_featured", nullable = false)
  private Boolean isFeatured = false;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  @Column(name = "display_order")
  private Integer displayOrder = 0;

  @Column(name = "rating_avg")
  private Double ratingAvg = 0.0;

  @Column(name = "rating_count")
  private Integer ratingCount = 0;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<ProductVariant> variants;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<ProductImage> images;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Review> reviews;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
    if (hasVariants == null) hasVariants = false;
    if (isFeatured == null) isFeatured = false;
    if (isActive == null) isActive = true;
    if (stockQuantity == null) stockQuantity = 0;
    if (displayOrder == null) displayOrder = 0;
    if (ratingAvg == null) ratingAvg = 0.0;
    if (ratingCount == null) ratingCount = 0;
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}