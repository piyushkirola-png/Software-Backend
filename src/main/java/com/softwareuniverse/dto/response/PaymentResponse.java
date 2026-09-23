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
public class PaymentResponse {
  private Long id;
  private Long orderId;
  private String orderNumber;
  private String gateway;
  private String gatewayOrderId;
  private String gatewayPaymentId;
  private String paymentLink;
  private BigDecimal amount;
  private String currency;
  private String status;
  private String failureReason;
  private String rawResponse;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
