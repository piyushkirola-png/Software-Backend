package com.softwareuniverse.repository;

import com.softwareuniverse.entity.Payment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

  List<Payment> findByOrderId(Long orderId);

  Optional<Payment> findByGatewayPaymentId(String gatewayPaymentId);

  Optional<Payment> findByGatewayOrderId(String gatewayOrderId);
}
