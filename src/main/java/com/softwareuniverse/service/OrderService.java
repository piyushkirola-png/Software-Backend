package com.softwareuniverse.service;

import com.softwareuniverse.dto.request.CheckoutRequest;
import com.softwareuniverse.dto.response.OrderResponse;
import java.util.List;
import org.springframework.data.domain.Page;

public interface OrderService {
  OrderResponse checkout(Long userId, CheckoutRequest request);

  List<OrderResponse> getMyOrders(Long userId);

  Page<OrderResponse> getMyOrdersPaginated(Long userId, int page, int size);

  Page<OrderResponse> getMyOrdersFiltered(
    Long userId,
    int page,
    int size,
    String orderNumber,
    String status,
    java.math.BigDecimal minTotal,
    java.math.BigDecimal maxTotal
  );

  byte[] exportMyOrdersCsv(
    Long userId,
    String orderNumber,
    String status,
    java.math.BigDecimal minTotal,
    java.math.BigDecimal maxTotal
  );

  OrderResponse getOrderById(Long userId, Long orderId);

  OrderResponse getOrderByNumber(Long userId, String orderNumber);
}
