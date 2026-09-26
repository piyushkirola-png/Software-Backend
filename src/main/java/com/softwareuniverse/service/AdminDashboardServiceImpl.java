package com.softwareuniverse.service;

import com.softwareuniverse.dto.response.DashboardStatsResponse;
import com.softwareuniverse.dto.response.GstReportResponse;
import com.softwareuniverse.dto.response.RecentOrderResponse;
import com.softwareuniverse.dto.response.SalesReportResponse;
import com.softwareuniverse.entity.*;
import com.softwareuniverse.repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

  private final UserRepository userRepository;
  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final ReviewRepository reviewRepository;
  private final LicenseKeyRepository licenseKeyRepository;
  private final InvoiceRepository invoiceRepository;
  private final OrderItemRepository orderItemRepository;

  @Override
  @Transactional(readOnly = true)
  public DashboardStatsResponse getDashboardStats() {
    LocalDateTime todayStart = LocalDate.now().atStartOfDay();
    LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

    List<Order> allOrders = orderRepository.findAll();

    BigDecimal totalRevenue = allOrders
      .stream()
      .filter(o -> o.getStatus() == OrderStatus.SUCCESS)
      .map(Order::getTotal)
      .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal todayRevenue = allOrders
      .stream()
      .filter(o -> o.getStatus() == OrderStatus.SUCCESS)
      .filter(
        o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(todayStart)
      )
      .map(Order::getTotal)
      .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal monthRevenue = allOrders
      .stream()
      .filter(o -> o.getStatus() == OrderStatus.SUCCESS)
      .filter(
        o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(monthStart)
      )
      .map(Order::getTotal)
      .reduce(BigDecimal.ZERO, BigDecimal::add);

    long todayOrders = allOrders
      .stream()
      .filter(
        o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(todayStart)
      )
      .count();

    long pendingOrders = allOrders
      .stream()
      .filter(o -> o.getStatus() == OrderStatus.PENDING)
      .count();
    long successOrders = allOrders
      .stream()
      .filter(o -> o.getStatus() == OrderStatus.SUCCESS)
      .count();
    long failedOrders = allOrders
      .stream()
      .filter(o -> o.getStatus() == OrderStatus.FAILED)
      .count();

    long totalUsers = userRepository.countByRole(Role.USER);
    long todayNewUsers = userRepository
      .findAll()
      .stream()
      .filter(
        u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(todayStart)
      )
      .count();

    long totalProducts = productRepository.count();
    long totalCategories = categoryRepository.count();
    long activeProducts = productRepository.countByIsActiveTrue();

    long pendingReviews = reviewRepository
      .findByIsApprovedFalseOrderByCreatedAtDesc(PageRequest.of(0, 1))
      .getTotalElements();
    long totalReviews = reviewRepository.count();

    long availableKeys = licenseKeyRepository.countByStatus(
      KeyStatus.AVAILABLE
    );
    long reservedKeys = licenseKeyRepository.countByStatus(KeyStatus.RESERVED);
    long soldKeys = licenseKeyRepository.countByStatus(KeyStatus.SOLD);
    long revokedKeys = licenseKeyRepository.countByStatus(KeyStatus.REVOKED);

    // Revenue by category
    List<DashboardStatsResponse.CategoryRevenue> revenueByCategory =
      orderItemRepository
        .sumRevenueByCategory()
        .stream()
        .map(row ->
          DashboardStatsResponse.CategoryRevenue.builder()
            .categoryName((String) row[0])
            .revenue(row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO)
            .build()
        )
        .toList();

    // Top 5 selling products
    List<DashboardStatsResponse.TopProduct> topSellingProducts =
      orderItemRepository
        .findTopSellingProducts(PageRequest.of(0, 5))
        .stream()
        .map(row ->
          DashboardStatsResponse.TopProduct.builder()
            .productId((Long) row[0])
            .productTitle((String) row[1])
            .unitsSold(row[2] != null ? ((Number) row[2]).longValue() : 0L)
            .revenue(row[3] != null ? (BigDecimal) row[3] : BigDecimal.ZERO)
            .build()
        )
        .toList();

    return DashboardStatsResponse.builder()
      .totalRevenue(totalRevenue)
      .todayRevenue(todayRevenue)
      .monthRevenue(monthRevenue)
      .totalOrders(allOrders.size())
      .todayOrders(todayOrders)
      .pendingOrders(pendingOrders)
      .successOrders(successOrders)
      .failedOrders(failedOrders)
      .totalUsers(totalUsers)
      .todayNewUsers(todayNewUsers)
      .totalProducts(totalProducts)
      .totalCategories(totalCategories)
      .activeProducts(activeProducts)
      .pendingReviews(pendingReviews)
      .totalReviews(totalReviews)
      .availableKeys(availableKeys)
      .reservedKeys(reservedKeys)
      .soldKeys(soldKeys)
      .revokedKeys(revokedKeys)
      .revenueByCategory(revenueByCategory)
      .topSellingProducts(topSellingProducts)
      .build();
  }

  @Override
  @Transactional(readOnly = true)
  public List<RecentOrderResponse> getRecentOrders(int limit) {
    var page = orderRepository.findAll(
      PageRequest.of(0, limit, Sort.by("createdAt").descending())
    );

    return page
      .getContent()
      .stream()
      .map(o ->
        RecentOrderResponse.builder()
          .id(o.getId())
          .orderNumber(o.getOrderNumber())
          .customerName(o.getUser().getName())
          .customerEmail(o.getCustomerEmail())
          .total(o.getTotal())
          .status(o.getStatus().name())
          .createdAt(o.getCreatedAt())
          .build()
      )
      .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public SalesReportResponse getSalesReport(
    LocalDate fromDate,
    LocalDate toDate
  ) {
    if (fromDate == null) fromDate = LocalDate.now().minusDays(30);
    if (toDate == null) toDate = LocalDate.now();

    LocalDateTime from = fromDate.atStartOfDay();
    LocalDateTime to = toDate.atTime(LocalTime.MAX);

    List<Order> orders = orderRepository
      .findAll()
      .stream()
      .filter(o -> o.getStatus() == OrderStatus.SUCCESS)
      .filter(o -> o.getCreatedAt() != null)
      .filter(
        o -> !o.getCreatedAt().isBefore(from) && !o.getCreatedAt().isAfter(to)
      )
      .toList();

    BigDecimal totalRevenue = orders
      .stream()
      .map(Order::getTotal)
      .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal totalDiscount = orders
      .stream()
      .map(o -> o.getDiscount() != null ? o.getDiscount() : BigDecimal.ZERO)
      .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal totalTax = orders
      .stream()
      .map(o -> o.getTax() != null ? o.getTax() : BigDecimal.ZERO)
      .reduce(BigDecimal.ZERO, BigDecimal::add);

    // Daily breakdown
    Map<LocalDate, List<Order>> byDay = new TreeMap<>();
    for (Order o : orders) {
      LocalDate d = o.getCreatedAt().toLocalDate();
      byDay.computeIfAbsent(d, k -> new ArrayList<>()).add(o);
    }

    List<SalesReportResponse.DailySales> daily = byDay
      .entrySet()
      .stream()
      .map(e ->
        SalesReportResponse.DailySales.builder()
          .date(e.getKey())
          .orders(e.getValue().size())
          .revenue(
            e
              .getValue()
              .stream()
              .map(Order::getTotal)
              .reduce(BigDecimal.ZERO, BigDecimal::add)
          )
          .build()
      )
      .toList();

    return SalesReportResponse.builder()
      .fromDate(fromDate)
      .toDate(toDate)
      .totalOrders(orders.size())
      .totalRevenue(totalRevenue)
      .totalDiscount(totalDiscount)
      .totalTax(totalTax)
      .dailyBreakdown(daily)
      .build();
  }

  @Override
  @Transactional(readOnly = true)
  public GstReportResponse getGstReport(LocalDate fromDate, LocalDate toDate) {
    if (fromDate == null) fromDate = LocalDate.now().minusDays(30);
    if (toDate == null) toDate = LocalDate.now();

    LocalDateTime from = fromDate.atStartOfDay();
    LocalDateTime to = toDate.atTime(LocalTime.MAX);

    List<Invoice> invoices = invoiceRepository
      .findAll()
      .stream()
      .filter(i -> i.getGeneratedAt() != null)
      .filter(
        i ->
          !i.getGeneratedAt().isBefore(from) && !i.getGeneratedAt().isAfter(to)
      )
      .toList();

    BigDecimal totalTaxableValue = BigDecimal.ZERO;
    BigDecimal totalCgst = BigDecimal.ZERO;
    BigDecimal totalSgst = BigDecimal.ZERO;
    BigDecimal totalIgst = BigDecimal.ZERO;
    BigDecimal totalTax = BigDecimal.ZERO;
    BigDecimal totalValue = BigDecimal.ZERO;

    List<GstReportResponse.InvoiceLine> lines = new ArrayList<>();

    for (Invoice inv : invoices) {
      BigDecimal taxable = inv
        .getSubtotal()
        .subtract(
          inv.getDiscount() != null ? inv.getDiscount() : BigDecimal.ZERO
        );
      totalTaxableValue = totalTaxableValue.add(taxable);
      totalCgst = totalCgst.add(nvl(inv.getCgst()));
      totalSgst = totalSgst.add(nvl(inv.getSgst()));
      totalIgst = totalIgst.add(nvl(inv.getIgst()));
      totalTax = totalTax.add(nvl(inv.getTotalTax()));
      totalValue = totalValue.add(inv.getTotal());

      lines.add(
        GstReportResponse.InvoiceLine.builder()
          .invoiceNumber(inv.getInvoiceNumber())
          .invoiceDate(inv.getGeneratedAt().toLocalDate())
          .buyerName(inv.getBuyerName())
          .buyerState(inv.getBuyerState())
          .taxableValue(taxable)
          .cgst(nvl(inv.getCgst()))
          .sgst(nvl(inv.getSgst()))
          .igst(nvl(inv.getIgst()))
          .total(inv.getTotal())
          .build()
      );
    }

    return GstReportResponse.builder()
      .fromDate(fromDate)
      .toDate(toDate)
      .totalInvoices(invoices.size())
      .totalTaxableValue(totalTaxableValue)
      .totalCgst(totalCgst)
      .totalSgst(totalSgst)
      .totalIgst(totalIgst)
      .totalTax(totalTax)
      .totalInvoiceValue(totalValue)
      .invoices(lines)
      .build();
  }

  private BigDecimal nvl(BigDecimal b) {
    return b != null ? b : BigDecimal.ZERO;
  }
}
