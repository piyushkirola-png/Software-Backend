package com.softwareuniverse.controller;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import com.softwareuniverse.common.response.ApiResponse;
import com.softwareuniverse.dto.request.ApplyCouponRequest;
import com.softwareuniverse.dto.response.CouponResponse;
import com.softwareuniverse.entity.User;
import com.softwareuniverse.repository.UserRepository;
import com.softwareuniverse.service.CouponService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

  private final CouponService couponService;
  private final UserRepository userRepository;

  /**
   * Validate coupon against an order subtotal (passed in query for preview). e.g. POST
   * /api/coupons/validate?subtotal=500
   */
  @PostMapping("/validate")
  public ResponseEntity<ApiResponse<CouponResponse>> validate(
      Principal principal,
      @Valid @RequestBody ApplyCouponRequest request,
      @RequestParam BigDecimal subtotal) {
    Long userId = principal != null ? currentUserId(principal) : null;
    CouponResponse coupon = couponService.validateCoupon(request.getCode(), subtotal, userId);
    return ResponseEntity.ok(ApiResponse.success("Coupon valid", coupon));
  }

  private Long currentUserId(Principal principal) {
    User user =
        userRepository
            .findByEmail(principal.getName())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return user.getId();
  }
}
