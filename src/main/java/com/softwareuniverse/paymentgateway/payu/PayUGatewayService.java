package com.softwareuniverse.paymentgateway.payu;

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
public class PayUGatewayService implements PaymentGateway {

  @Override
  public String getGatewayKey() {
    return "PAYU";
  }

  @Override
  public PaymentInitiateResponse initiate(Order order, Payment payment) {
    log.info("PayU: initiate payment for order {}", order.getOrderNumber());

    String txnId = "payu_" + System.currentTimeMillis();
    payment.setGatewayOrderId(txnId);

    Map<String, Object> gatewayData = new HashMap<>();
    gatewayData.put("txnid", txnId);
    gatewayData.put("amount", order.getTotal());
    gatewayData.put("productinfo", "Software License");
    gatewayData.put("firstname", order.getUser().getName());
    gatewayData.put("email", order.getCustomerEmail());
    gatewayData.put("phone", order.getCustomerPhone());

    return PaymentInitiateResponse.builder()
        .paymentId(payment.getId())
        .gateway(getGatewayKey())
        .gatewayOrderId(txnId)
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