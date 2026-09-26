package com.softwareuniverse.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
  name = "license_keys",
  indexes = {
    @Index(name = "idx_key_status", columnList = "status"),
    @Index(name = "idx_key_product", columnList = "product_id"),
  }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LicenseKey {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "variant_id")
  private ProductVariant variant;

  @Column(name = "license_key", nullable = false, unique = true, length = 255)
  private String licenseKey;

  @Enumerated(EnumType.STRING)
  @Column(
    name = "status",
    nullable = false,
    length = 20,
    columnDefinition = "VARCHAR(20)"
  )
  private KeyStatus status = KeyStatus.AVAILABLE;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id")
  private Order order;

  @Column(name = "batch_name", length = 100)
  private String batchName;

  @Column(name = "notes", length = 500)
  private String notes;

  @Column(name = "reserved_at")
  private LocalDateTime reservedAt;

  @Column(name = "sold_at")
  private LocalDateTime soldAt;

  @Column(name = "uploaded_at")
  private LocalDateTime uploadedAt;

  @PrePersist
  protected void onCreate() {
    uploadedAt = LocalDateTime.now();
    if (status == null) status = KeyStatus.AVAILABLE;
  }
}
