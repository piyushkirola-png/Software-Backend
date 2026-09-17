package com.softwareuniverse.paymentgateway.cashfree;

import com.softwareuniverse.dto.response.PaymentInitiateResponse;
import com.softwareuniverse.entity.Order;
import com.softwareuniverse.entity.Payment;
import com.softwareuniverse.paymentgateway.PaymentGateway;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CashfreeGatewayService implements PaymentGateway {

  @Override
  public String getGatewayKey() {
    return "CASHFREE";
  }

  @Override
  public PaymentInitiateResponse initiate(Order order, Payment payment) {
    log.info("Cashfree: initiate payment for order {}", order.getOrderNumber());

    String mockOrderId = "cf_order_" + System.currentTimeMillis();
    payment.setGatewayOrderId(mockOrderId);

    Map<String, Object> gatewayData = new HashMap<>();
    gatewayData.put("payment_session_id", "cf_session_" + System.currentTimeMillis());
    gatewayData.put("order_id", mockOrderId);

    return PaymentInitiateResponse.builder()
        .paymentId(payment.getId())
        .gateway(getGatewayKey())
        .gatewayOrderId(mockOrderId)
        .amount(order.getTotal())
        .currency("INR")
        .status(payment.getStatus().name())
        .gatewayData(gatewayData)
        .build();
  }

  @Override
  public boolean verifyReturn(Map<String, String> params) {
    return true;
  }
}