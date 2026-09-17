package com.softwareuniverse.service;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import com.softwareuniverse.dto.request.CheckoutRequest;
import com.softwareuniverse.dto.response.OrderItemResponse;
import com.softwareuniverse.dto.response.OrderResponse;
import com.softwareuniverse.entity.*;
import com.softwareuniverse.repository.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final UserRepository userRepository;
  private final AddressRepository addressRepository;
  private final ProductRepository productRepository;
  private final LicenseKeyRepository licenseKeyRepository;
  private final CouponService couponService;
  private final InvoiceRepository invoiceRepository;

  private static final BigDecimal GST_RATE = new BigDecimal("0.18"); // 18% GST
  private static final DateTimeFormatter ORDER_DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

  @Override
  @Transactional
  public OrderResponse checkout(Long userId, CheckoutRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    Address address =
        addressRepository
            .findByIdAndUserId(request.getAddressId(), userId)
            .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

    Cart cart =
        cartRepository
            .findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

    List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
    if (cartItems.isEmpty()) {
      throw new RuntimeException("Cart is empty");
    }

    // 1. Reserve one license key per item (variant-aware)
    for (CartItem item : cartItems) {
      reserveKey(item);
    }

    // 2. Compute subtotal
    BigDecimal subtotal =
        cartItems.stream()
            .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    // 3. Apply coupon (if any)
    BigDecimal discount = BigDecimal.ZERO;
    if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
      discount = couponService.calculateDiscount(request.getCouponCode(), subtotal, userId);
    }

    BigDecimal afterDiscount = subtotal.subtract(discount);

    // 4. Compute tax (GST inclusive — display only)
    BigDecimal tax = afterDiscount.multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP);

    BigDecimal total = afterDiscount.setScale(2, RoundingMode.HALF_UP);

    // 5. Create order
    Order order = new Order();
    order.setOrderNumber(generateOrderNumber());
    order.setUser(user);
    order.setAddress(address);
    order.setSubtotal(subtotal);
    order.setDiscount(discount);
    order.setCouponCode(request.getCouponCode());
    order.setTax(tax);
    order.setTotal(total);
    order.setStatus(OrderStatus.PENDING);
    order.setCustomerEmail(user.getEmail());
    order.setCustomerPhone(address.getPhone());
    orderRepository.save(order);

    // 6. Create order items (also link reserved keys to this order)
    for (CartItem item : cartItems) {
      OrderItem orderItem = new OrderItem();
      orderItem.setOrder(order);
      orderItem.setProduct(item.getProduct());
      orderItem.setVariant(item.getVariant());
      orderItem.setProductTitle(item.getProduct().getTitle());
      orderItem.setVariantName(item.getVariant() != null ? item.getVariant().getVariantName() : null);
      orderItem.setQuantity(item.getQuantity());
      orderItem.setUnitPrice(item.getUnitPrice());
      orderItem.setLineTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
      orderItemRepository.save(orderItem);
    }

    // 7. Clear cart
    cartItemRepository.deleteByCartId(cart.getId());

    // 8. Record coupon usage (only if SUCCESS later — for now record at checkout; adjust if needed)
    // We'll record on payment success in PaymentService, not here.

    return toResponse(order);
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrderResponse> getMyOrders(Long userId) {
    return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public Page<OrderResponse> getMyOrdersPaginated(Long userId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable).map(this::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public OrderResponse getOrderById(Long userId, Long orderId) {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    if (!order.getUser().getId().equals(userId)) {
      throw new RuntimeException("Unauthorized");
    }
    return toResponse(order);
  }

  @Override
  @Transactional(readOnly = true)
  public OrderResponse getOrderByNumber(Long userId, String orderNumber) {
    Order order =
        orderRepository
            .findByOrderNumber(orderNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    if (!order.getUser().getId().equals(userId)) {
      throw new RuntimeException("Unauthorized");
    }
    return toResponse(order);
  }

  // ============ Helpers ============

  private void reserveKey(CartItem item) {
    LicenseKey key;
    if (item.getVariant() != null) {
      key =
          licenseKeyRepository
              .findFirstByProductIdAndVariantIdAndStatus(
                  item.getProduct().getId(), item.getVariant().getId(), KeyStatus.AVAILABLE)
              .orElseThrow(
                  () ->
                      new RuntimeException(
                          "Out of stock for: "
                              + item.getProduct().getTitle()
                              + " ("
                              + item.getVariant().getVariantName()
                              + ")"));
    } else {
      key =
          licenseKeyRepository
              .findFirstByProductIdAndStatus(item.getProduct().getId(), KeyStatus.AVAILABLE)
              .orElseThrow(
                  () -> new RuntimeException("Out of stock for: " + item.getProduct().getTitle()));
    }
    key.setStatus(KeyStatus.RESERVED);
    key.setReservedAt(LocalDateTime.now());
    licenseKeyRepository.save(key);
  }

  private String generateOrderNumber() {
    String date = LocalDateTime.now().format(ORDER_DATE_FMT);
    int rand = 1000 + new Random().nextInt(9000);
    return "SU-" + date + "-" + rand;
  }

  private OrderResponse toResponse(Order order) {
    List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());

    // Fetch assigned license keys (only on SUCCESS orders)
    List<LicenseKey> keys = licenseKeyRepository.findByOrderId(order.getId());

    List<OrderItemResponse> itemResponses =
        items.stream()
            .map(
                item -> {
                  String licenseKey = null;
                  if (OrderStatus.SUCCESS.equals(order.getStatus())) {
                    licenseKey =
                        keys.stream()
                            .filter(
                                k ->
                                    k.getProduct().getId().equals(item.getProduct().getId())
                                        && ((k.getVariant() == null && item.getVariant() == null)
                                            || (k.getVariant() != null
                                                && item.getVariant() != null
                                                && k.getVariant().getId().equals(item.getVariant().getId()))))
                            .map(LicenseKey::getLicenseKey)
                            .findFirst()
                            .orElse(null);
                  }
                  return OrderItemResponse.builder()
                      .id(item.getId())
                      .productId(item.getProduct().getId())
                      .productTitle(item.getProductTitle())
                      .productSlug(item.getProduct().getSlug())
                      .thumbnailUrl(item.getProduct().getThumbnailUrl())
                      .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                      .variantName(item.getVariantName())
                      .quantity(item.getQuantity())
                      .unitPrice(item.getUnitPrice())
                      .lineTotal(item.getLineTotal())
                      .licenseKey(licenseKey)
                      .build();
                })
            .toList();

    // Invoice info if exists
    String invoiceNumber = null;
    String invoiceUrl = null;
    var invoice = invoiceRepository.findByOrderId(order.getId()).orElse(null);
    if (invoice != null) {
      invoiceNumber = invoice.getInvoiceNumber();
      invoiceUrl = invoice.getPdfPath();
    }

    return OrderResponse.builder()
        .id(order.getId())
        .orderNumber(order.getOrderNumber())
        .userId(order.getUser().getId())
        .customerName(order.getUser().getName())
        .customerEmail(order.getCustomerEmail())
        .customerPhone(order.getCustomerPhone())
        .subtotal(order.getSubtotal())
        .discount(order.getDiscount())
        .couponCode(order.getCouponCode())
        .tax(order.getTax())
        .total(order.getTotal())
        .status(order.getStatus().name())
        .invoiceNumber(invoiceNumber)
        .invoicePdfUrl(invoiceUrl)
        .createdAt(order.getCreatedAt())
        .items(itemResponses)
        .build();
  }
}