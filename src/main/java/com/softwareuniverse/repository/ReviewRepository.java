package com.softwareuniverse.repository;

import com.softwareuniverse.entity.Review;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

  Page<Review> findByProductIdAndIsApprovedTrueOrderByCreatedAtDesc(
      Long productId, Pageable pageable);

  List<Review> findTop10ByIsApprovedTrueAndIsVerifiedPurchaseTrueOrderByCreatedAtDesc();

  Page<Review> findByIsApprovedFalseOrderByCreatedAtDesc(Pageable pageable);

  boolean existsByProductIdAndUserId(Long productId, Long userId);

  long countByProductIdAndIsApprovedTrue(Long productId);
}
