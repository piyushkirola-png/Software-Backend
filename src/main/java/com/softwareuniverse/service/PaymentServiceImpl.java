package com.softwareuniverse.service;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import com.softwareuniverse.dto.request.PaymentRequest;
import com.softwareuniverse.dto.response.GstBreakdown;
import com.softwareuniverse.dto.response.PaymentInitiateResponse;
import com.softwareuniverse.dto.response.PaymentResponse;
import com.softwareuniverse.entity.*;
import com.softwareuniverse.paymentgateway.cashfree.CashfreeGatewayService;
import com.softwareuniverse.repository.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

  private final PaymentRepository paymentRepository;
  private final OrderRepository orderRepository;
  private final LicenseKeyRepository licenseKeyRepository;
  private final InvoiceRepository invoiceRepository;
  private final CouponService couponService;
  private final InvoiceNumberService invoiceNumberService;
  private final InvoicePdfService invoicePdfService;
  private final EmailService emailService;
  private final GstCalculatorService gstCalculatorService;

  // Concrete gateway services (Astrology-style)
  private final CashfreeGatewayService cashfreeGatewayService;

  @Override
  @Transactional
  public PaymentInitiateResponse initiatePayment(Long userId, PaymentRequest request) {
    Order order =
        orderRepository
            .findById(request.getOrderId())
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

    if (!order.getUser().getId().equals(userId)) {
      throw new RuntimeException("Unauthorized");
    }
    if (order.getStatus() != OrderStatus.PENDING) {
      throw new RuntimeException("Order is not in PENDING state");
    }

    // Normalise gateway key
    String gatewayKey =
        request.getGateway() != null ? request.getGateway().toUpperCase().trim() : "CASHFREE";

    // Create payment record
    Payment payment = new Payment();
    payment.setOrder(order);
    payment.setUser(order.getUser());
    payment.setGateway(gatewayKey);
    payment.setAmount(order.getTotal());
    payment.setCurrency("INR");
    payment.setStatus(PaymentStatus.PENDING);

    // Generate a unique gateway order id (used by most gateways)
    String gatewayOrderId = "SU-" + order.getOrderNumber() + "-" + System.currentTimeMillis();
    payment.setGatewayOrderId(gatewayOrderId);

    paymentRepository.save(payment);

    // Dispatch to the concrete gateway
    boolean ok =
        switch (gatewayKey) {
          case "CASHFREE" -> cashfreeGatewayService.initiate(payment, order.getUser());
          default -> throw new RuntimeException("Unsupported gateway: " + gatewayKey);
        };

    if (!ok || payment.getPaymentLink() == null || payment.getPaymentLink().isBlank()) {
      payment.setStatus(PaymentStatus.FAILED);
      payment.setFailureReason("Unable to create payment link");
      paymentRepository.save(payment);
      throw new RuntimeException("Unable to create payment link. Please try again.");
    }

    // Save the link that the gateway service wrote
    paymentRepository.save(payment);

    log.info(
        "Payment initiated: order={}, gateway={}, paymentId={}, link={}",
        order.getOrderNumber(),
        gatewayKey,
        payment.getId(),
        payment.getPaymentLink());

    // Build response for frontend
    return PaymentInitiateResponse.builder()
        .paymentId(payment.getId())
        .gateway(payment.getGateway())
        .gatewayOrderId(payment.getGatewayOrderId())
        .amount(payment.getAmount())
        .currency(payment.getCurrency())
        .status(payment.getStatus().name())
        .gatewayData(
            java.util.Map.of(
                "paymentLink", payment.getPaymentLink() != null ? payment.getPaymentLink() : ""))
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public PaymentResponse getPaymentById(Long userId, Long paymentId) {
    Payment payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    if (!payment.getUser().getId().equals(userId)) {
      throw new RuntimeException("Unauthorized");
    }
    return toResponse(payment);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PaymentResponse> getPaymentsForOrder(Long userId, Long orderId) {
    return paymentRepository.findByOrderId(orderId).stream()
        .filter(p -> p.getUser().getId().equals(userId))
        .map(this::toResponse)
        .toList();
  }

  @Override
  @Transactional
  public void markSuccess(String gatewayPaymentId, String gatewayOrderId, String rawResponse) {
    Payment payment =
        paymentRepository
            .findByGatewayOrderId(gatewayOrderId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Payment not found for gateway order"));

    if (payment.getStatus() == PaymentStatus.SUCCESS) {
      log.info("Payment already marked SUCCESS — skipping");
      return;
    }

    payment.setStatus(PaymentStatus.SUCCESS);
    payment.setGatewayPaymentId(gatewayPaymentId);
    payment.setRawResponse(rawResponse);
    paymentRepository.save(payment);

    Order order = payment.getOrder();
    order.setStatus(OrderStatus.SUCCESS);
    orderRepository.save(order);

    List<LicenseKey> keys = licenseKeyRepository.findByOrderId(order.getId());
    for (LicenseKey key : keys) {
      key.setStatus(KeyStatus.SOLD);
      key.setSoldAt(LocalDateTime.now());
    }
    licenseKeyRepository.saveAll(keys);

    if (order.getCouponCode() != null && !order.getCouponCode().isBlank()) {
      try {
        couponService.recordUsage(order.getCouponCode(), order.getUser().getId(), order.getId());
      } catch (Exception e) {
        log.warn("Failed to record coupon usage: {}", e.getMessage());
      }
    }

    Invoice invoice = null;
    try {
      invoice = generateInvoice(order);
    } catch (Exception e) {
      log.error(
          "Invoice generation failed for order {}: {}", order.getOrderNumber(), e.getMessage(), e);
    }

    try {
      emailService.sendOrderConfirmation(order, keys, invoice);
    } catch (Exception e) {
      log.error("Failed to send order confirmation email: {}", e.getMessage(), e);
    }

    log.info("Payment SUCCESS: order={}, payment={}", order.getOrderNumber(), payment.getId());
  }

  @Override
  @Transactional
  public void markFailed(String gatewayOrderId, String reason) {
    Payment payment =
        paymentRepository
            .findByGatewayOrderId(gatewayOrderId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Payment not found for gateway order"));

    if (payment.getStatus() == PaymentStatus.FAILED) return;

    payment.setStatus(PaymentStatus.FAILED);
    payment.setFailureReason(reason);
    paymentRepository.save(payment);

    Order order = payment.getOrder();
    order.setStatus(OrderStatus.FAILED);
    orderRepository.save(order);

    List<LicenseKey> keys = licenseKeyRepository.findByOrderId(order.getId());
    for (LicenseKey key : keys) {
      key.setStatus(KeyStatus.AVAILABLE);
      key.setOrder(null);
      key.setReservedAt(null);
    }
    licenseKeyRepository.saveAll(keys);

    log.info("Payment FAILED: order={}, reason={}", order.getOrderNumber(), reason);
  }

  @Override
  @Transactional
  public PaymentResponse simulateSuccess(Long userId, Long paymentId) {
    Payment payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

    if (!payment.getUser().getId().equals(userId)) {
      throw new RuntimeException("Unauthorized");
    }

    markSuccess(
        "SIMULATED_" + System.currentTimeMillis(),
        payment.getGatewayOrderId(),
        "{\"simulated\":true}");

    return toResponse(paymentRepository.findById(paymentId).orElseThrow());
  }

  private Invoice generateInvoice(Order order) {
    Invoice invoice = new Invoice();
    invoice.setInvoiceNumber(invoiceNumberService.generateNextInvoiceNumber());
    invoice.setOrder(order);
    invoice.setUser(order.getUser());
    invoice.setSubtotal(order.getSubtotal());
    invoice.setDiscount(order.getDiscount());

    String buyerState = order.getAddress() != null ? order.getAddress().getState() : null;

    GstBreakdown gst =
        gstCalculatorService.calculate(
            order
                .getSubtotal()
                .subtract(order.getDiscount() != null ? order.getDiscount() : BigDecimal.ZERO),
            buyerState);

    invoice.setCgst(gst.getCgst());
    invoice.setSgst(gst.getSgst());
    invoice.setIgst(gst.getIgst());
    invoice.setTotalTax(gst.getTotalTax());
    invoice.setTotal(order.getTotal());
    invoice.setBuyerName(order.getUser().getName());
    invoice.setBuyerEmail(order.getCustomerEmail());
    invoice.setBuyerPhone(order.getCustomerPhone());

    if (order.getAddress() != null) {
      var a = order.getAddress();
      String addr =
          (a.getAddressLine1() != null ? a.getAddressLine1() : "")
              + (a.getAddressLine2() != null ? ", " + a.getAddressLine2() : "")
              + ", "
              + a.getCity()
              + ", "
              + a.getState()
              + " - "
              + a.getPincode();
      invoice.setBuyerAddress(addr);
      invoice.setBuyerState(a.getState());
    }

    invoiceRepository.save(invoice);

    String pdfPath = invoicePdfService.generateInvoicePdf(invoice);
    invoice.setPdfPath(pdfPath);
    invoiceRepository.save(invoice);

    log.info(
        "Invoice {} generated for order {}", invoice.getInvoiceNumber(), order.getOrderNumber());

    return invoice;
  }

  private PaymentResponse toResponse(Payment p) {
    return PaymentResponse.builder()
        .id(p.getId())
        .orderId(p.getOrder().getId())
        .orderNumber(p.getOrder().getOrderNumber())
        .gateway(p.getGateway())
        .gatewayOrderId(p.getGatewayOrderId())
        .gatewayPaymentId(p.getGatewayPaymentId())
        .paymentLink(p.getPaymentLink())
        .amount(p.getAmount())
        .currency(p.getCurrency())
        .status(p.getStatus().name())
        .failureReason(p.getFailureReason())
        .rawResponse(p.getRawResponse())
        .createdAt(p.getCreatedAt())
        .updatedAt(p.getUpdatedAt())
        .build();
  }
}
