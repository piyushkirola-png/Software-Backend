package com.softwareuniverse.service;

import com.softwareuniverse.dto.request.CouponRequest;
import com.softwareuniverse.dto.response.CouponResponse;
import org.springframework.data.domain.Page;

public interface AdminCouponService {

  Page<CouponResponse> getAllCoupons(int page, int size, String status);

  CouponResponse getCoupon(Long id);

  CouponResponse createCoupon(CouponRequest request);

  CouponResponse updateCoupon(Long id, CouponRequest request);

  void deleteCoupon(Long id);

  CouponResponse toggleActive(Long id);
}
