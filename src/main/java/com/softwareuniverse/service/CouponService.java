package com.softwareuniverse.service;

import com.softwareuniverse.dto.response.CouponResponse;
import java.math.BigDecimal;

public interface CouponService {

  /** Validate a coupon against an order subtotal. Returns the coupon if valid. */
  CouponResponse validateCoupon(String code, BigDecimal orderSubtotal, Long userId);

  /** Compute the discount amount for a valid coupon. */
  BigDecimal calculateDiscount(String code, BigDecimal orderSubtotal, Long userId);

  /** Increment usedCount + record CouponUsage. */
  void recordUsage(String code, Long userId, Long orderId);
}