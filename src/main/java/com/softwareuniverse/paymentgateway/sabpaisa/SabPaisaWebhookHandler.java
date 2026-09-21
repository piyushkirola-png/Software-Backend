package com.softwareuniverse.paymentgateway.sabpaisa;

import com.softwareuniverse.paymentgateway.WebhookHandler;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SabPaisaWebhookHandler implements WebhookHandler {

  @Override
  public String getGatewayKey() {
    return "SABPAISA";
  }

  @Override
  public void handle(String rawBody, Map<String, String> headers) {
    log.info("SabPaisa webhook received. Body length: {}", rawBody != null ? rawBody.length() : 0);
  }
}
