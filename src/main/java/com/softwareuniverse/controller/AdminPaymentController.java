package com.softwareuniverse.controller;

import com.softwareuniverse.common.response.ApiResponse;
import com.softwareuniverse.dto.response.PaymentResponse;
import com.softwareuniverse.service.AdminPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaymentController {

  private final AdminPaymentService adminPaymentService;

  @GetMapping
  public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getAll(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String gateway,
      @RequestParam(required = false) String search) {
    return ResponseEntity.ok(
        ApiResponse.success(
            "Payments fetched",
            adminPaymentService.getAllPayments(page, size, status, gateway, search)));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<PaymentResponse>> get(@PathVariable Long id) {
    return ResponseEntity.ok(
        ApiResponse.success("Payment fetched", adminPaymentService.getPayment(id)));
  }
}