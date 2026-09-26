package com.softwareuniverse.service;

import com.softwareuniverse.dto.request.PaymentRequest;
import com.softwareuniverse.dto.response.PaymentInitiateResponse;
import com.softwareuniverse.dto.response.PaymentResponse;
import java.util.List;

public interface PaymentService {

  PaymentInitiateResponse initiatePayment(Long userId, PaymentRequest request);

  PaymentResponse getPaymentById(Long userId, Long paymentId);

  List<PaymentResponse> getPaymentsForOrder(Long userId, Long orderId);

  void markSuccess(String gatewayPaymentId, String gatewayOrderId, String rawResponse);

  void markFailed(String gatewayOrderId, String reason);

  PaymentResponse simulateSuccess(Long userId, Long paymentId);
}
