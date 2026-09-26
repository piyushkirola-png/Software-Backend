package com.softwareuniverse.repository;

import com.softwareuniverse.entity.OrderItem;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
  List<OrderItem> findByOrderId(Long orderId);

  /**
   * Sum revenue grouped by product category, for SUCCESS orders only.
   */
  @Query(
    "SELECT p.category.name, SUM(oi.lineTotal) " +
      "FROM OrderItem oi " +
      "JOIN oi.product p " +
      "JOIN oi.order o " +
      "WHERE o.status = com.softwareuniverse.entity.OrderStatus.SUCCESS " +
      "GROUP BY p.category.name " +
      "ORDER BY SUM(oi.lineTotal) DESC"
  )
  List<Object[]> sumRevenueByCategory();

  /**
   * Top-selling products by units sold, for SUCCESS orders only.
   * */
  @Query(
    "SELECT oi.product.id, oi.product.title, SUM(oi.quantity), SUM(oi.lineTotal) " +
      "FROM OrderItem oi " +
      "JOIN oi.order o " +
      "WHERE o.status = com.softwareuniverse.entity.OrderStatus.SUCCESS " +
      "GROUP BY oi.product.id, oi.product.title " +
      "ORDER BY SUM(oi.quantity) DESC"
  )
  List<Object[]> findTopSellingProducts(Pageable pageable);
}
