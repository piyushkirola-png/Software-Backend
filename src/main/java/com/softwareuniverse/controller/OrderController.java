package com.softwareuniverse.controller;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import com.softwareuniverse.common.response.ApiResponse;
import com.softwareuniverse.dto.request.CheckoutRequest;
import com.softwareuniverse.dto.response.OrderResponse;
import com.softwareuniverse.entity.User;
import com.softwareuniverse.repository.UserRepository;
import com.softwareuniverse.service.OrderService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;
  private final UserRepository userRepository;

  /** Checkout */
  @PostMapping("/checkout")
  public ResponseEntity<ApiResponse<OrderResponse>> checkout(
    Principal principal,
    @Valid @RequestBody CheckoutRequest request
  ) {
    Long userId = currentUserId(principal);
    return ResponseEntity.ok(
      ApiResponse.success(
        "Order created",
        orderService.checkout(userId, request)
      )
    );
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<OrderResponse>>> myOrders(
    Principal principal
  ) {
    Long userId = currentUserId(principal);
    return ResponseEntity.ok(
      ApiResponse.success("Orders fetched", orderService.getMyOrders(userId))
    );
  }

  @GetMapping("/paginated")
  public ResponseEntity<ApiResponse<Page<OrderResponse>>> myOrdersPaginated(
    Principal principal,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
  ) {
    Long userId = currentUserId(principal);
    return ResponseEntity.ok(
      ApiResponse.success(
        "Orders fetched",
        orderService.getMyOrdersPaginated(userId, page, size)
      )
    );
  }

  @GetMapping("/filtered")
  public ResponseEntity<ApiResponse<Page<OrderResponse>>> myOrdersFiltered(
    Principal principal,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(required = false) String orderNumber,
    @RequestParam(required = false) String status,
    @RequestParam(required = false) java.math.BigDecimal minTotal,
    @RequestParam(required = false) java.math.BigDecimal maxTotal
  ) {
    Long userId = currentUserId(principal);
    return ResponseEntity.ok(
      ApiResponse.success(
        "Orders fetched",
        orderService.getMyOrdersFiltered(
          userId,
          page,
          size,
          orderNumber,
          status,
          minTotal,
          maxTotal
        )
      )
    );
  }

  @GetMapping("/export-csv")
  public ResponseEntity<byte[]> exportCsv(
    Principal principal,
    @RequestParam(required = false) String orderNumber,
    @RequestParam(required = false) String status,
    @RequestParam(required = false) java.math.BigDecimal minTotal,
    @RequestParam(required = false) java.math.BigDecimal maxTotal
  ) {
    Long userId = currentUserId(principal);
    byte[] data = orderService.exportMyOrdersCsv(
      userId,
      orderNumber,
      status,
      minTotal,
      maxTotal
    );
    return ResponseEntity.ok()
      .header(
        org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=my-orders.csv"
      )
      .contentType(
        org.springframework.http.MediaType.parseMediaType("text/csv")
      )
      .body(data);
  }

  @GetMapping("/{orderId}")
  public ResponseEntity<ApiResponse<OrderResponse>> getById(
    Principal principal,
    @PathVariable Long orderId
  ) {
    Long userId = currentUserId(principal);
    return ResponseEntity.ok(
      ApiResponse.success(
        "Order fetched",
        orderService.getOrderById(userId, orderId)
      )
    );
  }

  @GetMapping("/number/{orderNumber}")
  public ResponseEntity<ApiResponse<OrderResponse>> getByNumber(
    Principal principal,
    @PathVariable String orderNumber
  ) {
    Long userId = currentUserId(principal);
    return ResponseEntity.ok(
      ApiResponse.success(
        "Order fetched",
        orderService.getOrderByNumber(userId, orderNumber)
      )
    );
  }

  private Long currentUserId(Principal principal) {
    if (principal == null) {
      throw new ResourceNotFoundException("Unauthorized — please login");
    }
    User user = userRepository
      .findByEmail(principal.getName())
      .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return user.getId();
  }
}
