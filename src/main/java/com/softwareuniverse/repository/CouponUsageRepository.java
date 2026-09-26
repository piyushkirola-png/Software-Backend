package com.softwareuniverse.repository;

import com.softwareuniverse.entity.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponUsageRepository
  extends JpaRepository<CouponUsage, Long>
{
  long countByCouponIdAndUserId(Long couponId, Long userId);
}
