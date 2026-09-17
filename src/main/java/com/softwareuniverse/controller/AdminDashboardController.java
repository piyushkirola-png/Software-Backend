package com.softwareuniverse.controller;

import com.softwareuniverse.common.response.ApiResponse;
import com.softwareuniverse.dto.response.DashboardStatsResponse;
import com.softwareuniverse.dto.response.GstReportResponse;
import com.softwareuniverse.dto.response.RecentOrderResponse;
import com.softwareuniverse.dto.response.SalesReportResponse;
import com.softwareuniverse.service.AdminDashboardService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

  private final AdminDashboardService dashboardService;

  @GetMapping("/stats")
  public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats() {
    return ResponseEntity.ok(
        ApiResponse.success("Dashboard stats fetched", dashboardService.getDashboardStats()));
  }

  @GetMapping("/recent-orders")
  public ResponseEntity<ApiResponse<List<RecentOrderResponse>>> recentOrders(
      @RequestParam(defaultValue = "10") int limit) {
    return ResponseEntity.ok(
        ApiResponse.success("Recent orders", dashboardService.getRecentOrders(limit)));
  }

  @GetMapping("/reports/sales")
  public ResponseEntity<ApiResponse<SalesReportResponse>> salesReport(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
    return ResponseEntity.ok(
        ApiResponse.success("Sales report", dashboardService.getSalesReport(fromDate, toDate)));
  }

  @GetMapping("/reports/gst")
  public ResponseEntity<ApiResponse<GstReportResponse>> gstReport(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
    return ResponseEntity.ok(
        ApiResponse.success("GST report", dashboardService.getGstReport(fromDate, toDate)));
  }
}