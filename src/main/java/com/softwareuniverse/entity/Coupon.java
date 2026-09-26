package com.softwareuniverse.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 50)
  private String code;

  @Column(length = 255)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
  private CouponType type;

  @Column(name = "value", nullable = false, precision = 10, scale = 2)
  private BigDecimal value;

  @Column(name = "min_order_amount", precision = 10, scale = 2)
  private BigDecimal minOrderAmount = BigDecimal.ZERO;

  @Column(name = "max_discount", precision = 10, scale = 2)
  private BigDecimal maxDiscount;

  @Column(name = "usage_limit")
  private Integer usageLimit;

  @Column(name = "used_count")
  private Integer usedCount = 0;

  @Column(name = "per_user_limit")
  private Integer perUserLimit = 1;

  @Column(name = "starts_at")
  private LocalDateTime startsAt;

  @Column(name = "expires_at")
  private LocalDateTime expiresAt;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
    if (isActive == null) isActive = true;
    if (usedCount == null) usedCount = 0;
    if (minOrderAmount == null) minOrderAmount = BigDecimal.ZERO;
    if (perUserLimit == null) perUserLimit = 1;
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
