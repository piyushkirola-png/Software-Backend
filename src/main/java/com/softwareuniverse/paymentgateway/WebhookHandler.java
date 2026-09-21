package com.softwareuniverse.paymentgateway;

import java.util.Map;

public interface WebhookHandler {

  /** Gateway key this handler serves. */
  String getGatewayKey();

  /** Raw body + headers + signature verification. */
  void handle(String rawBody, Map<String, String> headers);
}
