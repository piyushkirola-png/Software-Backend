package com.softwareuniverse.service;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import com.softwareuniverse.dto.response.PaymentResponse;
import com.softwareuniverse.entity.Payment;
import com.softwareuniverse.entity.PaymentStatus;
import com.softwareuniverse.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminPaymentServiceImpl implements AdminPaymentService {

  private final PaymentRepository paymentRepository;

  @Override
  @Transactional(readOnly = true)
  public Page<PaymentResponse> getAllPayments(
    int page,
    int size,
    String status,
    String gateway,
    String orderNumber,
    String paymentId,
    java.math.BigDecimal minAmount,
    java.math.BigDecimal maxAmount
  ) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

    PaymentStatus statusEnum = parseStatus(status);
    String gatewayParam = (gateway != null && !gateway.isBlank())
      ? gateway.trim()
      : null;
    String orderNumberParam = (orderNumber != null && !orderNumber.isBlank())
      ? orderNumber.trim()
      : null;
    String paymentIdParam = (paymentId != null && !paymentId.isBlank())
      ? paymentId.trim()
      : null;

    return paymentRepository
      .adminSearch(
        statusEnum,
        gatewayParam,
        orderNumberParam,
        paymentIdParam,
        minAmount,
        maxAmount,
        pageable
      )
      .map(this::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public byte[] exportPaymentsCsv(
    String status,
    String gateway,
    String orderNumber,
    String paymentId,
    java.math.BigDecimal minAmount,
    java.math.BigDecimal maxAmount
  ) {
    PaymentStatus statusEnum = parseStatus(status);
    String gatewayParam = (gateway != null && !gateway.isBlank())
      ? gateway.trim()
      : null;
    String orderNumberParam = (orderNumber != null && !orderNumber.isBlank())
      ? orderNumber.trim()
      : null;
    String paymentIdParam = (paymentId != null && !paymentId.isBlank())
      ? paymentId.trim()
      : null;

    // Fetch up to 10,000 rows (no pagination for CSV)
    Pageable all = PageRequest.of(0, 10_000, Sort.by("id").descending());
    List<Payment> payments = paymentRepository
      .adminSearch(
        statusEnum,
        gatewayParam,
        orderNumberParam,
        paymentIdParam,
        minAmount,
        maxAmount,
        all
      )
      .getContent();

    StringBuilder sb = new StringBuilder();
    sb.append(
      "Payment ID,Order Number,Gateway,Gateway Order ID,Gateway Payment ID,Amount,Currency,Status,Created At,Failure Reason\n"
    );

    java.time.format.DateTimeFormatter dateFmt =
      java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    for (Payment p : payments) {
      sb
        .append(csv(p.getId() != null ? String.valueOf(p.getId()) : ""))
        .append(',')
        .append(csv(p.getOrder() != null ? p.getOrder().getOrderNumber() : ""))
        .append(',')
        .append(csv(p.getGateway()))
        .append(',')
        .append(csv(p.getGatewayOrderId()))
        .append(',')
        .append(csv(p.getGatewayPaymentId()))
        .append(',')
        .append(p.getAmount() != null ? p.getAmount() : "")
        .append(',')
        .append(csv(p.getCurrency()))
        .append(',')
        .append(p.getStatus() != null ? p.getStatus().name() : "")
        .append(',')
        .append(
          csv(p.getCreatedAt() != null ? p.getCreatedAt().format(dateFmt) : "")
        )
        .append(',')
        .append(csv(p.getFailureReason()))
        .append('\n');
    }

    return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
  }

  private PaymentStatus parseStatus(String status) {
    if (
      status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)
    ) return null;
    try {
      return PaymentStatus.valueOf(status.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private String csv(String s) {
    if (s == null) return "";
    if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
      return "\"" + s.replace("\"", "\"\"") + "\"";
    }
    return s;
  }

  @Override
  @Transactional(readOnly = true)
  public PaymentResponse getPayment(Long id) {
    Payment p = paymentRepository
      .findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    return toResponse(p);
  }

  private PaymentResponse toResponse(Payment p) {
    return PaymentResponse.builder()
      .id(p.getId())
      .orderId(p.getOrder() != null ? p.getOrder().getId() : null)
      .orderNumber(p.getOrder() != null ? p.getOrder().getOrderNumber() : null)
      .gateway(p.getGateway())
      .gatewayOrderId(p.getGatewayOrderId())
      .gatewayPaymentId(p.getGatewayPaymentId())
      .paymentLink(p.getPaymentLink())
      .amount(p.getAmount())
      .currency(p.getCurrency())
      .status(p.getStatus() != null ? p.getStatus().name() : null)
      .failureReason(p.getFailureReason())
      .rawResponse(p.getRawResponse())
      .createdAt(p.getCreatedAt())
      .updatedAt(p.getUpdatedAt())
      .build();
  }
}
