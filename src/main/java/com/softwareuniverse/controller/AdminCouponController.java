package com.softwareuniverse.controller;

import com.softwareuniverse.common.response.ApiResponse;
import com.softwareuniverse.dto.request.CouponRequest;
import com.softwareuniverse.dto.response.CouponResponse;
import com.softwareuniverse.service.AdminCouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/coupons")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCouponController {

  private final AdminCouponService adminCouponService;

  @GetMapping
  public ResponseEntity<ApiResponse<Page<CouponResponse>>> getAll(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String status) {
    return ResponseEntity.ok(
        ApiResponse.success(
            "Coupons fetched", adminCouponService.getAllCoupons(page, size, status)));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<CouponResponse>> get(@PathVariable Long id) {
    return ResponseEntity.ok(
        ApiResponse.success("Coupon fetched", adminCouponService.getCoupon(id)));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<CouponResponse>> create(
      @Valid @RequestBody CouponRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success("Coupon created", adminCouponService.createCoupon(request)));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<CouponResponse>> update(
      @PathVariable Long id, @Valid @RequestBody CouponRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success("Coupon updated", adminCouponService.updateCoupon(id, request)));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
    adminCouponService.deleteCoupon(id);
    return ResponseEntity.ok(ApiResponse.success("Coupon deleted", null));
  }

  @PostMapping("/{id}/toggle-active")
  public ResponseEntity<ApiResponse<CouponResponse>> toggle(@PathVariable Long id) {
    return ResponseEntity.ok(
        ApiResponse.success("Coupon toggled", adminCouponService.toggleActive(id)));
  }
}
