package com.softwareuniverse.repository;

import com.softwareuniverse.entity.Order;
import com.softwareuniverse.entity.OrderStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
  Optional<Order> findByOrderNumber(String orderNumber);

  List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

  Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

  Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

  Page<Order> findByStatusOrderByCreatedAtDesc(
    OrderStatus status,
    Pageable pageable
  );

  long countByStatus(OrderStatus status);

  @Query(
    "SELECT o FROM Order o WHERE o.user.id = :userId " +
      "AND (:orderNumber IS NULL OR LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :orderNumber, '%'))) " +
      "AND (:status IS NULL OR o.status = :status) " +
      "AND (:minTotal IS NULL OR o.total >= :minTotal) " +
      "AND (:maxTotal IS NULL OR o.total <= :maxTotal)"
  )
  Page<Order> findUserOrdersFiltered(
    @Param("userId") Long userId,
    @Param("orderNumber") String orderNumber,
    @Param("status") OrderStatus status,
    @Param("minTotal") java.math.BigDecimal minTotal,
    @Param("maxTotal") java.math.BigDecimal maxTotal,
    Pageable pageable
  );
}
