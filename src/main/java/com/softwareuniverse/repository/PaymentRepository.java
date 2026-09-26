package com.softwareuniverse.repository;

import com.softwareuniverse.entity.Payment;
import com.softwareuniverse.entity.PaymentStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
  List<Payment> findByOrderId(Long orderId);

  Optional<Payment> findByGatewayPaymentId(String gatewayPaymentId);

  Optional<Payment> findByGatewayOrderId(String gatewayOrderId);

  /**
   * Admin listing with optional filters
   */
  @Query(
    "SELECT p FROM Payment p " +
      "LEFT JOIN p.order o " +
      "WHERE (:status IS NULL OR p.status = :status) " +
      "AND (:gateway IS NULL OR LOWER(p.gateway) = LOWER(:gateway)) " +
      "AND (:orderNumber IS NULL OR LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :orderNumber, '%'))) " +
      "AND (:paymentId IS NULL OR " +
      "     LOWER(p.gatewayOrderId) LIKE LOWER(CONCAT('%', :paymentId, '%')) " +
      "     OR LOWER(p.gatewayPaymentId) LIKE LOWER(CONCAT('%', :paymentId, '%'))) " +
      "AND (:minAmount IS NULL OR p.amount >= :minAmount) " +
      "AND (:maxAmount IS NULL OR p.amount <= :maxAmount) " +
      "ORDER BY p.id DESC"
  )
  Page<Payment> adminSearch(
    @Param("status") PaymentStatus status,
    @Param("gateway") String gateway,
    @Param("orderNumber") String orderNumber,
    @Param("paymentId") String paymentId,
    @Param("minAmount") java.math.BigDecimal minAmount,
    @Param("maxAmount") java.math.BigDecimal maxAmount,
    Pageable pageable
  );
}
