package com.softwareuniverse.paymentgateway.cashfree;

import com.softwareuniverse.paymentgateway.WebhookHandler;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CashfreeWebhookHandler implements WebhookHandler {

  @Override
  public String getGatewayKey() {
    return "CASHFREE";
  }

  @Override
  public void handle(String rawBody, Map<String, String> headers) {
    log.info("Cashfree webhook received. Body length: {}", rawBody != null ? rawBody.length() : 0);
  }
}
