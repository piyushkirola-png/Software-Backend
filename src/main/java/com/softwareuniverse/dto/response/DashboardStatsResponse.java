package com.softwareuniverse.dto.response;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
  // Revenue
  private BigDecimal totalRevenue;
  private BigDecimal todayRevenue;
  private BigDecimal monthRevenue;

  // Orders
  private long totalOrders;
  private long todayOrders;
  private long pendingOrders;
  private long successOrders;
  private long failedOrders;

  // Users
  private long totalUsers;
  private long todayNewUsers;

  // Catalog
  private long totalProducts;
  private long totalCategories;
  private long activeProducts;

  // Reviews
  private long pendingReviews;
  private long totalReviews;

  // License Keys
  private long availableKeys;
  private long reservedKeys;
  private long soldKeys;
  private long revokedKeys;
}
