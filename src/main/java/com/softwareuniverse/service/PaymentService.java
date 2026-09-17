package com.softwareuniverse.service;

import com.softwareuniverse.dto.request.PaymentRequest;
import com.softwareuniverse.dto.response.PaymentInitiateResponse;
import com.softwareuniverse.dto.response.PaymentResponse;
import java.util.List;

public interface PaymentService {

  PaymentInitiateResponse initiatePayment(Long userId, PaymentRequest request);

  PaymentResponse getPaymentById(Long userId, Long paymentId);

  List<PaymentResponse> getPaymentsForOrder(Long userId, Long orderId);

  /** Called by webhook / verify endpoint — marks payment + order SUCCESS. */
  void markSuccess(String gatewayPaymentId, String gatewayOrderId, String rawResponse);

  /** Called by webhook / return endpoint — marks payment + order FAILED. */
  void markFailed(String gatewayOrderId, String reason);

  /** Manually used in dev / testing — simulate success. */
  PaymentResponse simulateSuccess(Long userId, Long paymentId);
}