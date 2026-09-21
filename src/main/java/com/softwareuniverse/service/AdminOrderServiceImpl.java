package com.softwareuniverse.service;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import com.softwareuniverse.dto.response.OrderItemResponse;
import com.softwareuniverse.dto.response.OrderResponse;
import com.softwareuniverse.entity.*;
import com.softwareuniverse.repository.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminOrderServiceImpl implements AdminOrderService {

  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final LicenseKeyRepository licenseKeyRepository;
  private final InvoiceRepository invoiceRepository;
  private final EmailService emailService;
  private final InvoicePdfService invoicePdfService;

  @Value("${app.uploads.dir:uploads}")
  private String uploadsDir;

  @Override
  @Transactional(readOnly = true)
  public Page<OrderResponse> getAllOrders(int page, int size, String status, String search) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

    Page<Order> orders;
    if (status != null && !status.isBlank()) {
      OrderStatus s = OrderStatus.valueOf(status.toUpperCase());
      orders = orderRepository.findByStatusOrderByCreatedAtDesc(s, pageable);
    } else {
      orders = orderRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    // Optional search (order number, email, name)
    if (search != null && !search.isBlank()) {
      String q = search.toLowerCase();
      List<Order> filtered =
          orders.getContent().stream()
              .filter(
                  o ->
                      o.getOrderNumber().toLowerCase().contains(q)
                          || o.getCustomerEmail().toLowerCase().contains(q)
                          || o.getUser().getName().toLowerCase().contains(q))
              .toList();
      return new PageImpl<>(
          filtered.stream().map(this::toResponse).toList(), pageable, filtered.size());
    }

    return orders.map(this::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<OrderResponse> getOrdersByDateRange(
      int page, int size, LocalDate fromDate, LocalDate toDate, String status) {
    LocalDate from = fromDate != null ? fromDate : LocalDate.now().minusDays(30);
    LocalDate to = toDate != null ? toDate : LocalDate.now();

    LocalDateTime fromTs = from.atStartOfDay();
    LocalDateTime toTs = to.atTime(LocalTime.MAX);

    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

    List<Order> filtered =
        orderRepository.findAll().stream()
            .filter(o -> o.getCreatedAt() != null)
            .filter(o -> !o.getCreatedAt().isBefore(fromTs) && !o.getCreatedAt().isAfter(toTs))
            .filter(
                o ->
                    status == null
                        || status.isBlank()
                        || o.getStatus().name().equalsIgnoreCase(status))
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .toList();

    int start = Math.min(page * size, filtered.size());
    int end = Math.min(start + size, filtered.size());
    List<Order> pageContent = filtered.subList(start, end);

    return new PageImpl<>(
        pageContent.stream().map(this::toResponse).toList(), pageable, filtered.size());
  }

  @Override
  @Transactional(readOnly = true)
  public OrderResponse getOrder(Long id) {
    Order o =
        orderRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    return toResponse(o);
  }

  @Override
  @Transactional
  public OrderResponse updateStatus(Long id, String status) {
    Order o =
        orderRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

    OrderStatus newStatus;
    try {
      newStatus = OrderStatus.valueOf(status.toUpperCase());
    } catch (Exception e) {
      throw new RuntimeException("Invalid status. Use PENDING, SUCCESS, or FAILED.");
    }

    OrderStatus oldStatus = o.getStatus();
    o.setStatus(newStatus);
    orderRepository.save(o);

    // If moving PENDING → FAILED: release reserved keys back to AVAILABLE
    if (oldStatus == OrderStatus.PENDING && newStatus == OrderStatus.FAILED) {
      List<LicenseKey> keys = licenseKeyRepository.findByOrderId(o.getId());
      for (LicenseKey k : keys) {
        k.setStatus(KeyStatus.AVAILABLE);
        k.setOrder(null);
        k.setReservedAt(null);
      }
      licenseKeyRepository.saveAll(keys);
      log.info("Order {} marked FAILED — released {} keys", o.getOrderNumber(), keys.size());
    }

    // If moving PENDING → SUCCESS: mark keys SOLD (admin override)
    if (oldStatus == OrderStatus.PENDING && newStatus == OrderStatus.SUCCESS) {
      List<LicenseKey> keys = licenseKeyRepository.findByOrderId(o.getId());
      for (LicenseKey k : keys) {
        k.setStatus(KeyStatus.SOLD);
        k.setSoldAt(LocalDateTime.now());
      }
      licenseKeyRepository.saveAll(keys);
      log.info("Order {} manually marked SUCCESS — {} keys SOLD", o.getOrderNumber(), keys.size());
    }

    return toResponse(o);
  }

  @Override
  @Transactional
  public void resendOrderEmail(Long id) {
    Order o =
        orderRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

    if (o.getStatus() != OrderStatus.SUCCESS) {
      throw new RuntimeException("Can only resend email for SUCCESS orders");
    }

    List<LicenseKey> keys = licenseKeyRepository.findByOrderId(o.getId());
    Invoice invoice = invoiceRepository.findByOrderId(o.getId()).orElse(null);

    emailService.sendOrderConfirmation(o, keys, invoice);
    log.info("Order confirmation email re-sent for {}", o.getOrderNumber());
  }

  @Override
  @Transactional(readOnly = true)
  public byte[] getInvoicePdf(Long orderId) {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

    Invoice invoice =
        invoiceRepository
            .findByOrderId(orderId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "No invoice exists for this order. Invoices are generated only for SUCCESS orders."));

    if (invoice.getPdfPath() == null || invoice.getPdfPath().isBlank()) {
      throw new ResourceNotFoundException("Invoice PDF has not been generated for this order.");
    }

    try {
      // pdfPath is like "/uploads/invoices/SU-2026-27-0001.pdf"
      // Strip leading "/uploads/" → "invoices/SU-2026-27-0001.pdf"
      String relative = invoice.getPdfPath().replaceFirst("^/uploads/", "");
      Path filePath = Paths.get(uploadsDir, relative).toAbsolutePath();

      if (!Files.exists(filePath)) {
        log.warn("Invoice PDF missing on disk: {} — regenerating", filePath);
        invoicePdfService.generateInvoicePdf(invoice);
        // assume generator wrote to same path; re-read
        if (!Files.exists(filePath)) {
          throw new ResourceNotFoundException("Invoice PDF file is missing from storage.");
        }
      }

      return Files.readAllBytes(filePath);
    } catch (IOException e) {
      log.error("Failed to read invoice PDF for order {}: {}", orderId, e.getMessage(), e);
      throw new RuntimeException("Failed to read invoice PDF: " + e.getMessage());
    }
  }

  @Override
  @Transactional(readOnly = true)
  public byte[] exportOrdersCsv() {
    List<Order> orders = orderRepository.findAll(Sort.by("createdAt").descending());

    StringBuilder sb = new StringBuilder();
    sb.append("Order Number,Date,Customer Name,Customer Email,Customer Phone,");
    sb.append("Items Count,Subtotal,Discount,Tax,Total,Status\n");

    for (Order o : orders) {
      List<OrderItem> items = orderItemRepository.findByOrderId(o.getId());

      sb.append(csvEscape(o.getOrderNumber())).append(',');
      sb.append(o.getCreatedAt() != null ? o.getCreatedAt().toString() : "").append(',');
      sb.append(csvEscape(o.getUser().getName())).append(',');
      sb.append(csvEscape(o.getCustomerEmail())).append(',');
      sb.append(csvEscape(o.getCustomerPhone() != null ? o.getCustomerPhone() : "")).append(',');
      sb.append(items.size()).append(',');
      sb.append(o.getSubtotal()).append(',');
      sb.append(o.getDiscount() != null ? o.getDiscount() : BigDecimal.ZERO).append(',');
      sb.append(o.getTax() != null ? o.getTax() : BigDecimal.ZERO).append(',');
      sb.append(o.getTotal()).append(',');
      sb.append(o.getStatus().name()).append('\n');
    }

    return sb.toString().getBytes(StandardCharsets.UTF_8);
  }

  private String csvEscape(String s) {
    if (s == null) return "";
    if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
      return "\"" + s.replace("\"", "\"\"") + "\"";
    }
    return s;
  }

  // ============ Helpers ============

  private OrderResponse toResponse(Order o) {
    List<OrderItem> items = orderItemRepository.findByOrderId(o.getId());
    List<LicenseKey> keys = licenseKeyRepository.findByOrderId(o.getId());

    List<OrderItemResponse> itemResponses =
        items.stream()
            .map(
                item -> {
                  String licenseKey = null;
                  if (o.getStatus() == OrderStatus.SUCCESS) {
                    licenseKey =
                        keys.stream()
                            .filter(
                                k ->
                                    k.getProduct().getId().equals(item.getProduct().getId())
                                        && ((k.getVariant() == null && item.getVariant() == null)
                                            || (k.getVariant() != null
                                                && item.getVariant() != null
                                                && k.getVariant()
                                                    .getId()
                                                    .equals(item.getVariant().getId()))))
                            .map(LicenseKey::getLicenseKey)
                            .findFirst()
                            .orElse(null);
                  }
                  return OrderItemResponse.builder()
                      .id(item.getId())
                      .productId(item.getProduct().getId())
                      .productTitle(item.getProductTitle())
                      .productSlug(item.getProduct().getSlug())
                      .thumbnailUrl(item.getProduct().getThumbnailUrl())
                      .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                      .variantName(item.getVariantName())
                      .quantity(item.getQuantity())
                      .unitPrice(item.getUnitPrice())
                      .lineTotal(item.getLineTotal())
                      .licenseKey(licenseKey)
                      .build();
                })
            .toList();

    Invoice invoice = invoiceRepository.findByOrderId(o.getId()).orElse(null);

    return OrderResponse.builder()
        .id(o.getId())
        .orderNumber(o.getOrderNumber())
        .userId(o.getUser().getId())
        .customerName(o.getUser().getName())
        .customerEmail(o.getCustomerEmail())
        .customerPhone(o.getCustomerPhone())
        .subtotal(o.getSubtotal())
        .discount(o.getDiscount())
        .couponCode(o.getCouponCode())
        .tax(o.getTax())
        .total(o.getTotal())
        .status(o.getStatus().name())
        .invoiceNumber(invoice != null ? invoice.getInvoiceNumber() : null)
        .invoicePdfUrl(invoice != null ? invoice.getPdfPath() : null)
        .createdAt(o.getCreatedAt())
        .items(itemResponses)
        .build();
  }
}
