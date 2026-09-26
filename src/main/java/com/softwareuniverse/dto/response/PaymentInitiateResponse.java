package com.softwareuniverse.dto.response;

import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiateResponse {

  private Long paymentId;
  private String gateway;
  private String gatewayOrderId;
  private BigDecimal amount;
  private String currency;
  private String status;
  private Map<String, Object> gatewayData;
}
