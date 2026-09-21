package com.softwareuniverse.paymentgateway.sabpaisa;

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
public class SabPaisaGatewayService implements PaymentGateway {

  @Override
  public String getGatewayKey() {
    return "SABPAISA";
  }

  @Override
  public PaymentInitiateResponse initiate(Order order, Payment payment) {
    log.info("SabPaisa: initiate payment for order {}", order.getOrderNumber());

    String clientTxnId = "sp_" + System.currentTimeMillis();
    payment.setGatewayOrderId(clientTxnId);

    Map<String, Object> gatewayData = new HashMap<>();
    gatewayData.put("client_txn_id", clientTxnId);
    gatewayData.put("amount", order.getTotal());
    gatewayData.put("email", order.getCustomerEmail());
    gatewayData.put("phone", order.getCustomerPhone());

    return PaymentInitiateResponse.builder()
        .paymentId(payment.getId())
        .gateway(getGatewayKey())
        .gatewayOrderId(clientTxnId)
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
