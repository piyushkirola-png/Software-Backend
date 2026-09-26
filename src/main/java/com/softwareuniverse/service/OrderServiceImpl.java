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
  private final LicenseKeyRepository licenseKeyRepository;
  private final CouponService couponService;
  private final InvoiceRepository invoiceRepository;
  private final AdminKeyService adminKeyService;

  private static final BigDecimal GST_RATE = new BigDecimal("0.18"); // 18% GST
  private static final DateTimeFormatter ORDER_DATE_FMT =
    DateTimeFormatter.ofPattern("yyyyMMdd");

  @Override
  @Transactional
  public OrderResponse checkout(Long userId, CheckoutRequest request) {
    User user = userRepository
      .findById(userId)
      .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    Address address = addressRepository
      .findByIdAndUserId(request.getAddressId(), userId)
      .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

    Cart cart = cartRepository
      .findByUserId(userId)
      .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

    List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
    if (cartItems.isEmpty()) {
      throw new RuntimeException("Cart is empty");
    }

    BigDecimal subtotal = cartItems
      .stream()
      .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
      .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal discount = BigDecimal.ZERO;
    if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
      discount = couponService.calculateDiscount(
        request.getCouponCode(),
        subtotal,
        userId
      );
    }

    BigDecimal afterDiscount = subtotal.subtract(discount);

    BigDecimal tax = afterDiscount
      .multiply(GST_RATE)
      .setScale(2, RoundingMode.HALF_UP);

    BigDecimal total = afterDiscount.setScale(2, RoundingMode.HALF_UP);

    // 1) Create order FIRST so we can link keys to it
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
    order.setGstNumber(request.getGstNumber());
    order.setNotes(request.getNotes());
    orderRepository.save(order);

    // 2) Reserve keys — one per unit of quantity
    for (CartItem item : cartItems) {
      reserveKeys(item, order);
    }

    // 3) Sync stock for each affected product/variant
    for (CartItem item : cartItems) {
      try {
        adminKeyService.syncStock(
          item.getProduct().getId(),
          item.getVariant() != null ? item.getVariant().getId() : null
        );
      } catch (Exception e) {
        org.slf4j.LoggerFactory.getLogger(OrderServiceImpl.class).warn(
          "syncStock failed after reserve: {}",
          e.getMessage()
        );
      }
    }

    // 4) Create order items
    for (CartItem item : cartItems) {
      OrderItem orderItem = new OrderItem();
      orderItem.setOrder(order);
      orderItem.setProduct(item.getProduct());
      orderItem.setVariant(item.getVariant());
      orderItem.setProductTitle(item.getProduct().getTitle());
      orderItem.setVariantName(
        item.getVariant() != null ? item.getVariant().getVariantName() : null
      );
      orderItem.setQuantity(item.getQuantity());
      orderItem.setUnitPrice(item.getUnitPrice());
      orderItem.setLineTotal(
        item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
      );
      orderItemRepository.save(orderItem);
    }

    cartItemRepository.deleteByCartId(cart.getId());

    return toResponse(order);
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrderResponse> getMyOrders(Long userId) {
    return orderRepository
      .findByUserIdOrderByCreatedAtDesc(userId)
      .stream()
      .map(this::toResponse)
      .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public Page<OrderResponse> getMyOrdersPaginated(
    Long userId,
    int page,
    int size
  ) {
    Pageable pageable = PageRequest.of(
      page,
      size,
      Sort.by("createdAt").descending()
    );
    return orderRepository
      .findByUserIdOrderByCreatedAtDesc(userId, pageable)
      .map(this::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<OrderResponse> getMyOrdersFiltered(
    Long userId,
    int page,
    int size,
    String orderNumber,
    String status,
    BigDecimal minTotal,
    BigDecimal maxTotal
  ) {
    Pageable pageable = PageRequest.of(
      page,
      size,
      Sort.by("createdAt").descending()
    );

    OrderStatus statusEnum = null;
    if (
      status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)
    ) {
      try {
        statusEnum = OrderStatus.valueOf(status.toUpperCase());
      } catch (IllegalArgumentException ignored) {}
    }

    String orderNum = (orderNumber != null && !orderNumber.isBlank())
      ? orderNumber.trim()
      : null;

    return orderRepository
      .findUserOrdersFiltered(
        userId,
        orderNum,
        statusEnum,
        minTotal,
        maxTotal,
        pageable
      )
      .map(this::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public byte[] exportMyOrdersCsv(
    Long userId,
    String orderNumber,
    String status,
    BigDecimal minTotal,
    BigDecimal maxTotal
  ) {
    OrderStatus statusEnum = null;
    if (
      status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)
    ) {
      try {
        statusEnum = OrderStatus.valueOf(status.toUpperCase());
      } catch (IllegalArgumentException ignored) {}
    }

    String orderNum = (orderNumber != null && !orderNumber.isBlank())
      ? orderNumber.trim()
      : null;

    Pageable all = PageRequest.of(0, 10_000, Sort.by("createdAt").descending());
    List<Order> orders = orderRepository
      .findUserOrdersFiltered(
        userId,
        orderNum,
        statusEnum,
        minTotal,
        maxTotal,
        all
      )
      .getContent();

    StringBuilder sb = new StringBuilder();
    sb.append(
      "Order Number,Date,Items Count,Subtotal,Discount,Tax,Total,Status\n"
    );

    DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    for (Order o : orders) {
      long itemCount = orderItemRepository.findByOrderId(o.getId()).size();
      sb
        .append(csv(o.getOrderNumber()))
        .append(',')
        .append(
          csv(o.getCreatedAt() != null ? o.getCreatedAt().format(dateFmt) : "")
        )
        .append(',')
        .append(itemCount)
        .append(',')
        .append(o.getSubtotal() != null ? o.getSubtotal() : BigDecimal.ZERO)
        .append(',')
        .append(o.getDiscount() != null ? o.getDiscount() : BigDecimal.ZERO)
        .append(',')
        .append(o.getTax() != null ? o.getTax() : BigDecimal.ZERO)
        .append(',')
        .append(o.getTotal() != null ? o.getTotal() : BigDecimal.ZERO)
        .append(',')
        .append(o.getStatus() != null ? o.getStatus().name() : "")
        .append('\n');
    }

    return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
  }

  private String csv(String s) {
    if (s == null) return "";
    if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
      return "\"" + s.replace("\"", "\"\"") + "\"";
    }
    return s;
  }

  @Override
  @Transactional(readOnly = true)
  public OrderResponse getOrderById(Long userId, Long orderId) {
    Order order = orderRepository
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
    Order order = orderRepository
      .findByOrderNumber(orderNumber)
      .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    if (!order.getUser().getId().equals(userId)) {
      throw new RuntimeException("Unauthorized");
    }
    return toResponse(order);
  }

  private void reserveKeys(CartItem item, Order order) {
    int quantity = item.getQuantity() != null ? item.getQuantity() : 1;
    if (quantity < 1) quantity = 1;

    List<LicenseKey> available;
    if (item.getVariant() != null) {
      available = licenseKeyRepository.findByProductIdAndVariantIdAndStatus(
        item.getProduct().getId(),
        item.getVariant().getId(),
        KeyStatus.AVAILABLE
      );
    } else {
      available = licenseKeyRepository.findByProductIdAndStatus(
        item.getProduct().getId(),
        KeyStatus.AVAILABLE
      );
    }

    if (available.size() < quantity) {
      String suffix =
        item.getVariant() != null
          ? " (" + item.getVariant().getVariantName() + ")"
          : "";
      throw new RuntimeException(
        "Only " +
          available.size() +
          " key(s) available for: " +
          item.getProduct().getTitle() +
          suffix +
          ". Requested: " +
          quantity
      );
    }

    LocalDateTime now = LocalDateTime.now();
    List<LicenseKey> toReserve = available.subList(0, quantity);
    for (LicenseKey key : toReserve) {
      key.setStatus(KeyStatus.RESERVED);
      key.setReservedAt(now);
      key.setOrder(order);
    }
    licenseKeyRepository.saveAll(toReserve);
  }

  private String generateOrderNumber() {
    String date = LocalDateTime.now().format(ORDER_DATE_FMT);
    int rand = 1000 + new Random().nextInt(9000);
    return "SU-" + date + "-" + rand;
  }

  private OrderResponse toResponse(Order order) {
    List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());

    List<LicenseKey> keys = licenseKeyRepository.findByOrderId(order.getId());

    List<OrderItemResponse> itemResponses = items
      .stream()
      .map(item -> {
        java.util.List<String> licenseKeys = new java.util.ArrayList<>();
        if (OrderStatus.SUCCESS.equals(order.getStatus())) {
          licenseKeys = keys
            .stream()
            .filter(
              k ->
                k.getProduct().getId().equals(item.getProduct().getId()) &&
                ((k.getVariant() == null && item.getVariant() == null) ||
                  (k.getVariant() != null &&
                    item.getVariant() != null &&
                    k.getVariant().getId().equals(item.getVariant().getId())))
            )
            .map(LicenseKey::getLicenseKey)
            .toList();
        }
        String firstKey = licenseKeys.isEmpty() ? null : licenseKeys.get(0);
        return OrderItemResponse.builder()
          .id(item.getId())
          .productId(item.getProduct().getId())
          .productTitle(item.getProductTitle())
          .productSlug(item.getProduct().getSlug())
          .thumbnailUrl(item.getProduct().getThumbnailUrl())
          .variantId(
            item.getVariant() != null ? item.getVariant().getId() : null
          )
          .variantName(item.getVariantName())
          .quantity(item.getQuantity())
          .unitPrice(item.getUnitPrice())
          .lineTotal(item.getLineTotal())
          .licenseKey(firstKey)
          .licenseKeys(licenseKeys)
          .build();
      })
      .toList();

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
