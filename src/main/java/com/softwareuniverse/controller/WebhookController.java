package com.softwareuniverse.controller;

import com.softwareuniverse.common.response.ApiResponse;
import com.softwareuniverse.paymentgateway.WebhookHandler;
import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {

  private final List<WebhookHandler> handlers;

  @PostMapping("/{gateway}")
  public ResponseEntity<ApiResponse<Void>> handleWebhook(
      @PathVariable String gateway, HttpServletRequest request) {

    String rawBody = readBody(request);
    Map<String, String> headers = new HashMap<>();
    Enumeration<String> names = request.getHeaderNames();
    while (names.hasMoreElements()) {
      String name = names.nextElement();
      headers.put(name.toLowerCase(), request.getHeader(name));
    }

    WebhookHandler handler =
        handlers.stream()
            .filter(h -> h.getGatewayKey().equalsIgnoreCase(gateway))
            .findFirst()
            .orElse(null);

    if (handler == null) {
      log.warn("No webhook handler for gateway: {}", gateway);
      return ResponseEntity.ok(ApiResponse.success("Ignored", null));
    }

    try {
      handler.handle(rawBody, headers);
    } catch (Exception e) {
      log.error("Webhook handling failed for {}: {}", gateway, e.getMessage(), e);
    }

    return ResponseEntity.ok(ApiResponse.success("Webhook received", null));
  }

  private String readBody(HttpServletRequest request) {
    StringBuilder sb = new StringBuilder();
    try (BufferedReader reader = request.getReader()) {
      String line;
      while ((line = reader.readLine()) != null) sb.append(line);
    } catch (Exception e) {
      log.warn("Failed to read webhook body: {}", e.getMessage());
    }
    return sb.toString();
  }
}
