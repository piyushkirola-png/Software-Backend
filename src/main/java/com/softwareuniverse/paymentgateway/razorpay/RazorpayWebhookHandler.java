package com.softwareuniverse.paymentgateway.razorpay;

import com.softwareuniverse.paymentgateway.WebhookHandler;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Razorpay webhook handler — STUB. Real signature verification + event processing added later. */
@Slf4j
@Service
public class RazorpayWebhookHandler implements WebhookHandler {

  @Override
  public String getGatewayKey() {
    return "RAZORPAY";
  }

  @Override
  public void handle(String rawBody, Map<String, String> headers) {
    log.info("Razorpay webhook received. Body length: {}", rawBody != null ? rawBody.length() : 0);
    // TODO: verify signature header X-Razorpay-Signature
    // TODO: parse event: payment.captured / payment.failed
    // TODO: call PaymentService.markSuccess / markFailed
  }
}
