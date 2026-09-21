package com.softwareuniverse.service;

import com.softwareuniverse.dto.request.CheckoutRequest;
import com.softwareuniverse.dto.response.OrderResponse;
import java.util.List;
import org.springframework.data.domain.Page;

public interface OrderService {

  OrderResponse checkout(Long userId, CheckoutRequest request);

  List<OrderResponse> getMyOrders(Long userId);

  Page<OrderResponse> getMyOrdersPaginated(Long userId, int page, int size);

  OrderResponse getOrderById(Long userId, Long orderId);

  OrderResponse getOrderByNumber(Long userId, String orderNumber);
}
