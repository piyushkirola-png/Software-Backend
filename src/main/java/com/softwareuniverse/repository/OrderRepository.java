package com.softwareuniverse.repository;

import com.softwareuniverse.entity.Order;
import com.softwareuniverse.entity.OrderStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

  Optional<Order> findByOrderNumber(String orderNumber);

  List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

  Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

  Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

  Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

  long countByStatus(OrderStatus status);
}
