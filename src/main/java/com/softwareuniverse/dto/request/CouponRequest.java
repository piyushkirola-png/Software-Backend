package com.softwareuniverse.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CouponRequest {

  @NotBlank(message = "Coupon code is required")
  private String code;

  private String description;

  @NotNull(message = "Type is required")
  private String type; // PERCENT or FLAT

  @NotNull(message = "Value is required")
  private BigDecimal value;

  private BigDecimal minOrderAmount;
  private BigDecimal maxDiscount;
  private Integer usageLimit;
  private Integer perUserLimit;
  private LocalDateTime startsAt;
  private LocalDateTime expiresAt;
  private Boolean isActive;
}
