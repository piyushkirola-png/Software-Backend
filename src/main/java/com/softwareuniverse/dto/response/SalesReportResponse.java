package com.softwareuniverse.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesReportResponse {
  private LocalDate fromDate;
  private LocalDate toDate;
  private long totalOrders;
  private BigDecimal totalRevenue;
  private BigDecimal totalDiscount;
  private BigDecimal totalTax;
  private List<DailySales> dailyBreakdown;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DailySales {
    private LocalDate date;
    private long orders;
    private BigDecimal revenue;
  }
}