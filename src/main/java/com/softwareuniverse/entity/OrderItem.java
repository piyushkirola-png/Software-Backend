package com.softwareuniverse.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "variant_id")
  private ProductVariant variant;

  @Column(name = "product_title", nullable = false, length = 255)
  private String productTitle;

  @Column(name = "variant_name", length = 100)
  private String variantName;

  @Column(name = "quantity", nullable = false)
  private Integer quantity = 1;

  @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal unitPrice;

  @Column(name = "line_total", nullable = false, precision = 10, scale = 2)
  private BigDecimal lineTotal;

  @PrePersist
  protected void onCreate() {
    if (quantity == null) quantity = 1;
  }
}
