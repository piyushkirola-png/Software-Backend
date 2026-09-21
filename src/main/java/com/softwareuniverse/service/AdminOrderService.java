package com.softwareuniverse.service;

import com.softwareuniverse.dto.response.OrderResponse;
import java.time.LocalDate;
import org.springframework.data.domain.Page;

public interface AdminOrderService {

  Page<OrderResponse> getAllOrders(int page, int size, String status, String search);

  Page<OrderResponse> getOrdersByDateRange(
      int page, int size, LocalDate fromDate, LocalDate toDate, String status);

  OrderResponse getOrder(Long id);

  OrderResponse updateStatus(Long id, String status);

  /** Manually trigger a resend of the order confirmation email with keys + invoice. */
  void resendOrderEmail(Long id);

  /** Returns the PDF bytes of the invoice for the given order. Throws if no invoice exists. */
  byte[] getInvoicePdf(Long orderId);

  /** Returns CSV of all orders (all statuses), suitable for Excel download. */
  byte[] exportOrdersCsv();
}
