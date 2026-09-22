package com.softwareuniverse.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "gateway", nullable = false, length = 50)
  private String gateway;

  @Column(name = "gateway_order_id", length = 255)
  private String gatewayOrderId;

  @Column(name = "gateway_payment_id", length = 255)
  private String gatewayPaymentId;

  @Column(name = "gateway_signature", length = 500)
  private String gatewaySignature;

  @Column(name = "payment_link", length = 500)
  private String paymentLink;

  @Column(name = "amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal amount;

  @Column(name = "currency", length = 10)
  private String currency = "INR";

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
  private PaymentStatus status = PaymentStatus.PENDING;

  @Column(name = "failure_reason", length = 500)
  private String failureReason;

  @Column(name = "raw_response", columnDefinition = "TEXT")
  private String rawResponse;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
    if (status == null) status = PaymentStatus.PENDING;
    if (currency == null) currency = "INR";
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
