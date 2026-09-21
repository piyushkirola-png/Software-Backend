package com.softwareuniverse.service;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import com.softwareuniverse.dto.response.KeyResponse;
import com.softwareuniverse.entity.KeyStatus;
import com.softwareuniverse.entity.LicenseKey;
import com.softwareuniverse.entity.Order;
import com.softwareuniverse.entity.OrderStatus;
import com.softwareuniverse.repository.LicenseKeyRepository;
import com.softwareuniverse.repository.OrderRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KeyInventoryServiceImpl implements KeyInventoryService {

  private final LicenseKeyRepository licenseKeyRepository;
  private final OrderRepository orderRepository;

  @Override
  @Transactional(readOnly = true)
  public List<LicenseKey> findKeysByOrder(Long orderId) {
    return licenseKeyRepository.findByOrderId(orderId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<KeyResponse> getMyKeys(Long userId) {
    // Get all SUCCESS orders for this user → get all keys assigned to those orders
    List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);

    return orders.stream()
        .filter(o -> o.getStatus() == OrderStatus.SUCCESS)
        .flatMap(o -> licenseKeyRepository.findByOrderId(o.getId()).stream())
        .filter(k -> k.getStatus() == KeyStatus.SOLD)
        .map(this::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<KeyResponse> getKeysForOrder(Long userId, Long orderId) {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    if (!order.getUser().getId().equals(userId)) {
      throw new RuntimeException("Unauthorized");
    }
    return licenseKeyRepository.findByOrderId(orderId).stream().map(this::toResponse).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public long countAvailable(Long productId, Long variantId) {
    if (variantId != null) {
      return licenseKeyRepository.countByProductIdAndVariantIdAndStatus(
          productId, variantId, KeyStatus.AVAILABLE);
    }
    return licenseKeyRepository.countByProductIdAndStatus(productId, KeyStatus.AVAILABLE);
  }

  private KeyResponse toResponse(LicenseKey k) {
    return KeyResponse.builder()
        .id(k.getId())
        .productId(k.getProduct().getId())
        .productTitle(k.getProduct().getTitle())
        .variantId(k.getVariant() != null ? k.getVariant().getId() : null)
        .variantName(k.getVariant() != null ? k.getVariant().getVariantName() : null)
        .licenseKey(k.getLicenseKey())
        .status(k.getStatus().name())
        .soldAt(k.getSoldAt())
        .build();
  }
}
