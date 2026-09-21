package com.softwareuniverse.controller;

import com.softwareuniverse.common.response.ApiResponse;
import com.softwareuniverse.dto.response.OrderResponse;
import com.softwareuniverse.service.AdminOrderService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

  private final AdminOrderService adminOrderService;

  @GetMapping
  public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAll(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String search) {
    return ResponseEntity.ok(
        ApiResponse.success(
            "Orders fetched", adminOrderService.getAllOrders(page, size, status, search)));
  }

  @GetMapping("/date-range")
  public ResponseEntity<ApiResponse<Page<OrderResponse>>> getByDateRange(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate fromDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate toDate,
      @RequestParam(required = false) String status) {
    return ResponseEntity.ok(
        ApiResponse.success(
            "Orders fetched",
            adminOrderService.getOrdersByDateRange(page, size, fromDate, toDate, status)));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<OrderResponse>> get(@PathVariable Long id) {
    return ResponseEntity.ok(ApiResponse.success("Order fetched", adminOrderService.getOrder(id)));
  }

  @PutMapping("/{id}/status")
  public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
      @PathVariable Long id, @RequestParam String status) {
    return ResponseEntity.ok(
        ApiResponse.success("Order status updated", adminOrderService.updateStatus(id, status)));
  }

  @PostMapping("/{id}/resend-email")
  public ResponseEntity<ApiResponse<Void>> resendEmail(@PathVariable Long id) {
    adminOrderService.resendOrderEmail(id);
    return ResponseEntity.ok(ApiResponse.success("Email resent", null));
  }

  @GetMapping("/export")
  public ResponseEntity<byte[]> exportCsv() {
    byte[] csv = adminOrderService.exportOrdersCsv();
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=orders.csv")
        .contentType(MediaType.parseMediaType("text/csv"))
        .body(csv);
  }

  @GetMapping("/{id}/invoice")
  public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long id) {
    byte[] pdf = adminOrderService.getInvoicePdf(id);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + id + ".pdf")
        .contentType(MediaType.APPLICATION_PDF)
        .body(pdf);
  }
}
