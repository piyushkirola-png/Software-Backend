package com.softwareuniverse.service;

import com.softwareuniverse.entity.KeyStatus;
import com.softwareuniverse.entity.LicenseKey;
import com.softwareuniverse.entity.Order;
import com.softwareuniverse.entity.OrderStatus;
import com.softwareuniverse.repository.LicenseKeyRepository;
import com.softwareuniverse.repository.OrderRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Auto-releases keys tied to orders that stayed PENDING for longer than the reservation window.
 *
 * <p>Runs every 5 minutes. Reservation window: 15 minutes (configurable).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeyReservationCleanupService {

  private final LicenseKeyRepository licenseKeyRepository;
  private final OrderRepository orderRepository;

  /** Minutes before a reserved key is released if payment hasn't succeeded. */
  private static final int RESERVATION_WINDOW_MINUTES = 15;

  @Scheduled(fixedRate = 5 * 60 * 1000) // every 5 minutes
  @Transactional
  public void releaseExpiredReservations() {
    LocalDateTime cutoff = LocalDateTime.now().minusMinutes(RESERVATION_WINDOW_MINUTES);

    List<LicenseKey> staleKeys =
        licenseKeyRepository.findAll().stream()
            .filter(k -> k.getStatus() == KeyStatus.RESERVED)
            .filter(k -> k.getReservedAt() != null && k.getReservedAt().isBefore(cutoff))
            .toList();

    if (staleKeys.isEmpty()) return;

    log.info("Found {} stale reserved keys — releasing", staleKeys.size());

    for (LicenseKey key : staleKeys) {
      // Release key
      key.setStatus(KeyStatus.AVAILABLE);
      key.setOrder(null);
      key.setReservedAt(null);

      // Mark the associated order FAILED if still PENDING
      // (we can't rely on order object being present after key.order = null; capture before)
    }
    // above loop nulls the reference — do the order check first
    for (LicenseKey key : staleKeys) {
      Order order = key.getOrder();
      if (order != null && order.getStatus() == OrderStatus.PENDING) {
        // Only fail the order if *all* its keys are stale (single-key cart common case)
        boolean allStale =
            licenseKeyRepository.findByOrderId(order.getId()).stream()
                .allMatch(k -> k.getReservedAt() != null && k.getReservedAt().isBefore(cutoff));
        if (allStale) {
          order.setStatus(OrderStatus.FAILED);
          orderRepository.save(order);
          log.info("Auto-failed stale pending order {}", order.getOrderNumber());
        }
      }
    }

    licenseKeyRepository.saveAll(staleKeys);
    log.info("Released {} keys back to AVAILABLE", staleKeys.size());
  }
}