package com.softwareuniverse.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
  private String invoiceNumber;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false, unique = true)
  private Order order;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
  private BigDecimal subtotal;

  @Column(name = "discount", precision = 10, scale = 2)
  private BigDecimal discount = BigDecimal.ZERO;

  @Column(name = "cgst", precision = 10, scale = 2)
  private BigDecimal cgst = BigDecimal.ZERO;

  @Column(name = "sgst", precision = 10, scale = 2)
  private BigDecimal sgst = BigDecimal.ZERO;

  @Column(name = "igst", precision = 10, scale = 2)
  private BigDecimal igst = BigDecimal.ZERO;

  @Column(name = "total_tax", precision = 10, scale = 2)
  private BigDecimal totalTax = BigDecimal.ZERO;

  @Column(name = "total", nullable = false, precision = 10, scale = 2)
  private BigDecimal total;

  @Column(name = "buyer_name", length = 150)
  private String buyerName;

  @Column(name = "buyer_email", length = 150)
  private String buyerEmail;

  @Column(name = "buyer_phone", length = 20)
  private String buyerPhone;

  @Column(name = "buyer_address", columnDefinition = "TEXT")
  private String buyerAddress;

  @Column(name = "buyer_gstin", length = 20)
  private String buyerGstin;

  @Column(name = "buyer_state", length = 100)
  private String buyerState;

  @Column(name = "pdf_path", length = 500)
  private String pdfPath;

  @Column(name = "generated_at")
  private LocalDateTime generatedAt;

  @PrePersist
  protected void onCreate() {
    generatedAt = LocalDateTime.now();
    if (discount == null) discount = BigDecimal.ZERO;
    if (cgst == null) cgst = BigDecimal.ZERO;
    if (sgst == null) sgst = BigDecimal.ZERO;
    if (igst == null) igst = BigDecimal.ZERO;
    if (totalTax == null) totalTax = BigDecimal.ZERO;
  }
}