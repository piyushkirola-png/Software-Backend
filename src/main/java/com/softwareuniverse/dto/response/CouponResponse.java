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
public class CouponResponse {

  private Long id;
  private String code;
  private String description;
  private String type;
  private BigDecimal value;
  private BigDecimal minOrderAmount;
  private BigDecimal maxDiscount;
  private Integer usageLimit;
  private Integer usedCount;
  private Integer perUserLimit;
  private LocalDateTime startsAt;
  private LocalDateTime expiresAt;
  private Boolean isActive;
}
