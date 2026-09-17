package com.softwareuniverse.service;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import com.softwareuniverse.dto.response.CouponResponse;
import com.softwareuniverse.entity.Coupon;
import com.softwareuniverse.entity.CouponType;
import com.softwareuniverse.entity.CouponUsage;
import com.softwareuniverse.repository.CouponRepository;
import com.softwareuniverse.repository.CouponUsageRepository;
import com.softwareuniverse.repository.OrderRepository;
import com.softwareuniverse.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

  private final CouponRepository couponRepository;
  private final CouponUsageRepository couponUsageRepository;
  private final UserRepository userRepository;
  private final OrderRepository orderRepository;

  @Override
  @Transactional(readOnly = true)
  public CouponResponse validateCoupon(String code, BigDecimal orderSubtotal, Long userId) {
    Coupon coupon = fetchAndValidate(code, orderSubtotal, userId);
    return toResponse(coupon);
  }

  @Override
  @Transactional(readOnly = true)
  public BigDecimal calculateDiscount(String code, BigDecimal orderSubtotal, Long userId) {
    if (code == null || code.isBlank()) return BigDecimal.ZERO;
    Coupon coupon = fetchAndValidate(code, orderSubtotal, userId);
    return computeDiscount(coupon, orderSubtotal);
  }

  @Override
  @Transactional
  public void recordUsage(String code, Long userId, Long orderId) {
    if (code == null || code.isBlank()) return;
    Coupon coupon =
        couponRepository
            .findByCodeIgnoreCase(code)
            .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

    coupon.setUsedCount(coupon.getUsedCount() + 1);
    couponRepository.save(coupon);

    CouponUsage usage = new CouponUsage();
    usage.setCoupon(coupon);
    usage.setUser(userRepository.getReferenceById(userId));
    usage.setOrder(orderRepository.getReferenceById(orderId));
    couponUsageRepository.save(usage);
  }

  // ============ Helpers ============

  private Coupon fetchAndValidate(String code, BigDecimal subtotal, Long userId) {
    Coupon coupon =
        couponRepository
            .findByCodeIgnoreCase(code)
            .orElseThrow(() -> new ResourceNotFoundException("Invalid coupon code"));

    if (!Boolean.TRUE.equals(coupon.getIsActive())) {
      throw new RuntimeException("Coupon is not active");
    }

    LocalDateTime now = LocalDateTime.now();
    if (coupon.getStartsAt() != null && now.isBefore(coupon.getStartsAt())) {
      throw new RuntimeException("Coupon is not yet active");
    }
    if (coupon.getExpiresAt() != null && now.isAfter(coupon.getExpiresAt())) {
      throw new RuntimeException("Coupon has expired");
    }
    if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
      throw new RuntimeException("Coupon usage limit reached");
    }
    if (coupon.getMinOrderAmount() != null
        && subtotal.compareTo(coupon.getMinOrderAmount()) < 0) {
      throw new RuntimeException(
          "Minimum order amount ₹" + coupon.getMinOrderAmount() + " required");
    }
    if (coupon.getPerUserLimit() != null && userId != null) {
      long used = couponUsageRepository.countByCouponIdAndUserId(coupon.getId(), userId);
      if (used >= coupon.getPerUserLimit()) {
        throw new RuntimeException("You have already used this coupon");
      }
    }
    return coupon;
  }

  private BigDecimal computeDiscount(Coupon coupon, BigDecimal subtotal) {
    BigDecimal discount;
    if (coupon.getType() == CouponType.PERCENT) {
      discount =
          subtotal
              .multiply(coupon.getValue())
              .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    } else {
      discount = coupon.getValue();
    }
    if (coupon.getMaxDiscount() != null && discount.compareTo(coupon.getMaxDiscount()) > 0) {
      discount = coupon.getMaxDiscount();
    }
    if (discount.compareTo(subtotal) > 0) discount = subtotal;
    return discount.setScale(2, RoundingMode.HALF_UP);
  }

  private CouponResponse toResponse(Coupon c) {
    return CouponResponse.builder()
        .id(c.getId())
        .code(c.getCode())
        .description(c.getDescription())
        .type(c.getType().name())
        .value(c.getValue())
        .minOrderAmount(c.getMinOrderAmount())
        .maxDiscount(c.getMaxDiscount())
        .usageLimit(c.getUsageLimit())
        .usedCount(c.getUsedCount())
        .perUserLimit(c.getPerUserLimit())
        .startsAt(c.getStartsAt())
        .expiresAt(c.getExpiresAt())
        .isActive(c.getIsActive())
        .build();
  }
}