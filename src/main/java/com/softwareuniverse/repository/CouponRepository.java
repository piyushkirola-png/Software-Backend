package com.softwareuniverse.repository;

import com.softwareuniverse.entity.Coupon;
import com.softwareuniverse.entity.CouponType;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
  Optional<Coupon> findByCodeIgnoreCase(String code);

  boolean existsByCode(String code);

  @Query(
    "SELECT c FROM Coupon c WHERE " +
      "(:statusActive IS NULL OR c.isActive = :statusActive) " +
      "AND (:type IS NULL OR c.type = :type) " +
      "AND (:valueMin IS NULL OR c.value >= :valueMin) " +
      "AND (:valueMax IS NULL OR c.value <= :valueMax)"
  )
  Page<Coupon> findAdminCoupons(
    @Param("statusActive") Boolean statusActive,
    @Param("type") CouponType type,
    @Param("valueMin") BigDecimal valueMin,
    @Param("valueMax") BigDecimal valueMax,
    Pageable pageable
  );
}
