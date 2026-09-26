package com.softwareuniverse.service;

import com.softwareuniverse.dto.response.CouponResponse;
import java.math.BigDecimal;

public interface CouponService {
  CouponResponse validateCoupon(
    String code,
    BigDecimal orderSubtotal,
    Long userId
  );

  BigDecimal calculateDiscount(
    String code,
    BigDecimal orderSubtotal,
    Long userId
  );

  void recordUsage(String code, Long userId, Long orderId);
}
