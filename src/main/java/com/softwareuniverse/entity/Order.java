package com.softwareuniverse.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "order_number", nullable = false, unique = true, length = 50)
  private String orderNumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "address_id")
  private Address address;

  @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
  private BigDecimal subtotal;

  @Column(name = "discount", precision = 10, scale = 2)
  private BigDecimal discount = BigDecimal.ZERO;

  @Column(name = "coupon_code", length = 50)
  private String couponCode;

  @Column(name = "tax", precision = 10, scale = 2)
  private BigDecimal tax = BigDecimal.ZERO;

  @Column(name = "total", nullable = false, precision = 10, scale = 2)
  private BigDecimal total;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
  private OrderStatus status = OrderStatus.PENDING;

  @Column(name = "customer_email", nullable = false, length = 150)
  private String customerEmail;

  @Column(name = "customer_phone", length = 20)
  private String customerPhone;

  // ===== NEW =====
  @Column(name = "gst_number", length = 20)
  private String gstNumber;

  @Column(name = "notes", length = 500)
  private String notes;

  @OneToMany(
      mappedBy = "order",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<OrderItem> items = new ArrayList<>();

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
    if (status == null) status = OrderStatus.PENDING;
    if (discount == null) discount = BigDecimal.ZERO;
    if (tax == null) tax = BigDecimal.ZERO;
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
