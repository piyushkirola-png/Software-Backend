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
    @RequestParam(required = false) String orderNumber,
    @RequestParam(required = false) String paymentId,
    @RequestParam(required = false) java.math.BigDecimal minAmount,
    @RequestParam(required = false) java.math.BigDecimal maxAmount
  ) {
    return ResponseEntity.ok(
      ApiResponse.success(
        "Payments fetched",
        adminPaymentService.getAllPayments(
          page,
          size,
          status,
          gateway,
          orderNumber,
          paymentId,
          minAmount,
          maxAmount
        )
      )
    );
  }

  @GetMapping("/export-csv")
  public ResponseEntity<byte[]> exportCsv(
    @RequestParam(required = false) String status,
    @RequestParam(required = false) String gateway,
    @RequestParam(required = false) String orderNumber,
    @RequestParam(required = false) String paymentId,
    @RequestParam(required = false) java.math.BigDecimal minAmount,
    @RequestParam(required = false) java.math.BigDecimal maxAmount
  ) {
    byte[] data = adminPaymentService.exportPaymentsCsv(
      status,
      gateway,
      orderNumber,
      paymentId,
      minAmount,
      maxAmount
    );
    return ResponseEntity.ok()
      .header(
        org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=payments.csv"
      )
      .contentType(
        org.springframework.http.MediaType.parseMediaType("text/csv")
      )
      .body(data);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<PaymentResponse>> get(
    @PathVariable Long id
  ) {
    return ResponseEntity.ok(
      ApiResponse.success("Payment fetched", adminPaymentService.getPayment(id))
    );
  }
}
