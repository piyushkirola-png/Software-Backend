package com.softwareuniverse.service;

import com.softwareuniverse.dto.response.DashboardStatsResponse;
import com.softwareuniverse.dto.response.GstReportResponse;
import com.softwareuniverse.dto.response.RecentOrderResponse;
import com.softwareuniverse.dto.response.SalesReportResponse;
import java.time.LocalDate;
import java.util.List;

public interface AdminDashboardService {
  DashboardStatsResponse getDashboardStats();

  List<RecentOrderResponse> getRecentOrders(int limit);

  SalesReportResponse getSalesReport(LocalDate fromDate, LocalDate toDate);

  GstReportResponse getGstReport(LocalDate fromDate, LocalDate toDate);
}
