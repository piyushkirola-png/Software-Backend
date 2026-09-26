package com.softwareuniverse.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {

  private Long id;
  private String invoiceNumber;
  private Long orderId;
  private String orderNumber;
  private BigDecimal subtotal;
  private BigDecimal discount;
  private BigDecimal cgst;
  private BigDecimal sgst;
  private BigDecimal igst;
  private BigDecimal totalTax;
  private BigDecimal total;
  private String buyerName;
  private String buyerEmail;
  private String buyerPhone;
  private String buyerAddress;
  private String buyerGstin;
  private String buyerState;
  private String pdfUrl;
  private LocalDateTime generatedAt;
}
