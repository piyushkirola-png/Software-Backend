package com.softwareuniverse.paymentgateway.payu;

import com.softwareuniverse.paymentgateway.WebhookHandler;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PayUWebhookHandler implements WebhookHandler {

  @Override
  public String getGatewayKey() {
    return "PAYU";
  }

  @Override
  public void handle(String rawBody, Map<String, String> headers) {
    log.info("PayU webhook received. Body length: {}", rawBody != null ? rawBody.length() : 0);
  }
}
