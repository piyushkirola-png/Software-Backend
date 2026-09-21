package com.softwareuniverse.paymentgateway.razorpay;

import com.softwareuniverse.dto.response.PaymentInitiateResponse;
import com.softwareuniverse.entity.Order;
import com.softwareuniverse.entity.Payment;
import com.softwareuniverse.paymentgateway.PaymentGateway;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Razorpay integration — STUB. Real HTTP calls to Razorpay API will be added in a later batch. For
 * now, we simply return placeholder gatewayData.
 */
@Slf4j
@Service
public class RazorpayGatewayService implements PaymentGateway {

  @Override
  public String getGatewayKey() {
    return "RAZORPAY";
  }

  @Override
  public PaymentInitiateResponse initiate(Order order, Payment payment) {
    log.info("Razorpay: initiate payment for order {}", order.getOrderNumber());

    // TODO: call Razorpay Orders API → get real gatewayOrderId
    String mockGatewayOrderId = "rzp_order_" + System.currentTimeMillis();
    payment.setGatewayOrderId(mockGatewayOrderId);

    Map<String, Object> gatewayData = new HashMap<>();
    gatewayData.put("key", "rzp_test_XXXXXXXXXXXX");
    gatewayData.put(
        "amount", order.getTotal().multiply(java.math.BigDecimal.valueOf(100)).intValue());
    gatewayData.put("currency", "INR");
    gatewayData.put("order_id", mockGatewayOrderId);
    gatewayData.put("name", "Software Universe");

    return PaymentInitiateResponse.builder()
        .paymentId(payment.getId())
        .gateway(getGatewayKey())
        .gatewayOrderId(mockGatewayOrderId)
        .amount(order.getTotal())
        .currency("INR")
        .status(payment.getStatus().name())
        .gatewayData(gatewayData)
        .build();
  }

  @Override
  public boolean verifyReturn(Map<String, String> params) {
    // TODO: verify signature
    return true;
  }
}
