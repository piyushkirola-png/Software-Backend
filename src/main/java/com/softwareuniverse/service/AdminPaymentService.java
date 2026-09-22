package com.softwareuniverse.service;

import com.softwareuniverse.dto.response.PaymentResponse;
import org.springframework.data.domain.Page;

public interface AdminPaymentService {

  Page<PaymentResponse> getAllPayments(
      int page, int size, String status, String gateway, String search);

  PaymentResponse getPayment(Long id);
}
