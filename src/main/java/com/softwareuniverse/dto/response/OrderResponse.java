package com.softwareuniverse.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

  private Long id;
  private String orderNumber;
  private Long userId;
  private String customerName;
  private String customerEmail;
  private String customerPhone;
  private BigDecimal subtotal;
  private BigDecimal discount;
  private String couponCode;
  private BigDecimal tax;
  private BigDecimal total;
  private String status;
  private String invoiceNumber;
  private String invoicePdfUrl;
  private LocalDateTime createdAt;
  private List<OrderItemResponse> items;
}
