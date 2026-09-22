package com.softwareuniverse.service;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import com.softwareuniverse.dto.response.PaymentResponse;
import com.softwareuniverse.entity.Payment;
import com.softwareuniverse.entity.PaymentStatus;
import com.softwareuniverse.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
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
    String search
  ) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

    PaymentStatus statusEnum = null;
    if (status != null && !status.isBlank()) {
      String s = status.trim().toUpperCase();
      switch (s) {
        case "PENDING" -> statusEnum = PaymentStatus.PENDING;
        case "SUCCESS" -> statusEnum = PaymentStatus.SUCCESS;
        case "FAILED" -> statusEnum = PaymentStatus.FAILED;
        default -> statusEnum = null;
      }
    }

    String gatewayParam = (gateway != null && !gateway.isBlank())
      ? gateway.trim()
      : null;

    String searchParam = (search != null && !search.isBlank())
      ? search.trim()
      : null;

    return paymentRepository
      .adminSearch(statusEnum, gatewayParam, searchParam, pageable)
      .map(this::toResponse);
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
