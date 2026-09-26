package com.softwareuniverse.service;

import com.softwareuniverse.dto.response.DownloadResponse;
import com.softwareuniverse.entity.*;
import com.softwareuniverse.repository.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DownloadServiceImpl implements DownloadService {

  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;

  @Override
  @Transactional(readOnly = true)
  public List<DownloadResponse> getMyDownloads(Long userId) {
    List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(
      userId
    );

    // Deduplicate by productId — latest purchase wins
    Map<Long, DownloadResponse> byProduct = new LinkedHashMap<>();

    for (Order o : orders) {
      if (o.getStatus() != OrderStatus.SUCCESS) continue;
      for (OrderItem item : orderItemRepository.findByOrderId(o.getId())) {
        Product p = item.getProduct();
        if (
          p.getDownloadFilePath() == null || p.getDownloadFilePath().isBlank()
        ) continue;

        byProduct.putIfAbsent(
          p.getId(),
          DownloadResponse.builder()
            .productId(p.getId())
            .productTitle(p.getTitle())
            .slug(p.getSlug())
            .thumbnailUrl(p.getThumbnailUrl())
            .downloadUrl(p.getDownloadFilePath())
            .purchasedAt(o.getCreatedAt())
            .build()
        );
      }
    }

    return new ArrayList<>(byProduct.values());
  }
}
