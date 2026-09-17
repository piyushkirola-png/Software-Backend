package com.softwareuniverse.service;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import com.softwareuniverse.dto.request.CouponRequest;
import com.softwareuniverse.dto.response.CouponResponse;
import com.softwareuniverse.entity.Coupon;
import com.softwareuniverse.entity.CouponType;
import com.softwareuniverse.repository.CouponRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminCouponServiceImpl implements AdminCouponService {

  private final CouponRepository couponRepository;

  @Override
  @Transactional(readOnly = true)
  public Page<CouponResponse> getAllCoupons(int page, int size, String status) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<Coupon> coupons = couponRepository.findAll(pageable);

    if (status != null && !status.isBlank()) {
      boolean active = "active".equalsIgnoreCase(status);
      List<Coupon> filtered =
          coupons.getContent().stream()
              .filter(c -> Boolean.valueOf(active).equals(c.getIsActive()))
              .toList();
      return new PageImpl<>(filtered.stream().map(this::toResponse).toList(), pageable, filtered.size());
    }
    return coupons.map(this::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public CouponResponse getCoupon(Long id) {
    Coupon c =
        couponRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
    return toResponse(c);
  }

  @Override
  @Transactional
  public CouponResponse createCoupon(CouponRequest request) {
    String code = request.getCode().trim().toUpperCase();

    if (couponRepository.existsByCode(code)) {
      throw new RuntimeException("Coupon with this code already exists: " + code);
    }

    CouponType type = parseType(request.getType());

    Coupon c = new Coupon();
    c.setCode(code);
    c.setDescription(request.getDescription());
    c.setType(type);
    c.setValue(request.getValue());
    c.setMinOrderAmount(request.getMinOrderAmount() != null ? request.getMinOrderAmount() : BigDecimal.ZERO);
    c.setMaxDiscount(request.getMaxDiscount());
    c.setUsageLimit(request.getUsageLimit());
    c.setUsedCount(0);
    c.setPerUserLimit(request.getPerUserLimit() != null ? request.getPerUserLimit() : 1);
    c.setStartsAt(request.getStartsAt());
    c.setExpiresAt(request.getExpiresAt());
    c.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

    couponRepository.save(c);
    return toResponse(c);
  }

  @Override
  @Transactional
  public CouponResponse updateCoupon(Long id, CouponRequest request) {
    Coupon c =
        couponRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

    if (request.getCode() != null && !request.getCode().isBlank()) {
      String code = request.getCode().trim().toUpperCase();
      if (!code.equals(c.getCode()) && couponRepository.existsByCode(code)) {
        throw new RuntimeException("Coupon code already in use: " + code);
      }
      c.setCode(code);
    }

    if (request.getDescription() != null) c.setDescription(request.getDescription());
    if (request.getType() != null) c.setType(parseType(request.getType()));
    if (request.getValue() != null) c.setValue(request.getValue());
    if (request.getMinOrderAmount() != null) c.setMinOrderAmount(request.getMinOrderAmount());
    if (request.getMaxDiscount() != null) c.setMaxDiscount(request.getMaxDiscount());
    if (request.getUsageLimit() != null) c.setUsageLimit(request.getUsageLimit());
    if (request.getPerUserLimit() != null) c.setPerUserLimit(request.getPerUserLimit());
    if (request.getStartsAt() != null) c.setStartsAt(request.getStartsAt());
    if (request.getExpiresAt() != null) c.setExpiresAt(request.getExpiresAt());
    if (request.getIsActive() != null) c.setIsActive(request.getIsActive());

    couponRepository.save(c);
    return toResponse(c);
  }

  @Override
  @Transactional
  public void deleteCoupon(Long id) {
    Coupon c =
        couponRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
    couponRepository.delete(c);
  }

  @Override
  @Transactional
  public CouponResponse toggleActive(Long id) {
    Coupon c =
        couponRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
    c.setIsActive(!Boolean.TRUE.equals(c.getIsActive()));
    couponRepository.save(c);
    return toResponse(c);
  }

  // ============ Helpers ============

  private CouponType parseType(String t) {
    try {
      return CouponType.valueOf(t.toUpperCase());
    } catch (Exception e) {
      throw new RuntimeException("Invalid coupon type. Use PERCENT or FLAT.");
    }
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