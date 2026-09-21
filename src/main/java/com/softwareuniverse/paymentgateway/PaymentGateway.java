package com.softwareuniverse.paymentgateway;

import com.softwareuniverse.dto.response.PaymentInitiateResponse;
import com.softwareuniverse.entity.Order;
import com.softwareuniverse.entity.Payment;
import java.util.Map;

public interface PaymentGateway {

  /** Unique key for this gateway: RAZORPAY, CASHFREE, PAYU, SABPAISA */
  String getGatewayKey();

  /** Initiate payment — returns gateway checkout params / redirect URL. */
  PaymentInitiateResponse initiate(Order order, Payment payment);

  /** Verify a synchronous return (redirect) payload — must throw on invalid signature. */
  boolean verifyReturn(Map<String, String> params);
}
