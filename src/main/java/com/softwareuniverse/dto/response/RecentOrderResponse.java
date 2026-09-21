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
public class RecentOrderResponse {
  private Long id;
  private String orderNumber;
  private String customerName;
  private String customerEmail;
  private BigDecimal total;
  private String status;
  private LocalDateTime createdAt;
}
