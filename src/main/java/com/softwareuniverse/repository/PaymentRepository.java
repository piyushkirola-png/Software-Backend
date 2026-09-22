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
   * Admin listing with optional filters: - status : "SUCCESS" / "FAILED" / "INITIATED" / "PENDING"
   * or null - gateway : "CASHFREE" / "RAZORPAY" / "PAYU" / "SABPAISA" or null - search : matches
   * orderNumber OR gatewayOrderId OR gatewayPaymentId (case-insensitive, substring)
   */
  @Query(
      "SELECT p FROM Payment p "
          + "LEFT JOIN p.order o "
          + "WHERE (:status IS NULL OR p.status = :status) "
          + "AND (:gateway IS NULL OR LOWER(p.gateway) = LOWER(:gateway)) "
          + "AND (:search IS NULL OR "
          + "     LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :search, '%')) "
          + "     OR LOWER(p.gatewayOrderId) LIKE LOWER(CONCAT('%', :search, '%')) "
          + "     OR LOWER(p.gatewayPaymentId) LIKE LOWER(CONCAT('%', :search, '%'))) "
          + "ORDER BY p.id DESC")
  Page<Payment> adminSearch(
      @Param("status") PaymentStatus status,
      @Param("gateway") String gateway,
      @Param("search") String search,
      Pageable pageable);
}
