package com.softwareuniverse.service;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import com.softwareuniverse.dto.response.ReviewResponse;
import com.softwareuniverse.entity.Product;
import com.softwareuniverse.entity.Review;
import com.softwareuniverse.repository.ProductRepository;
import com.softwareuniverse.repository.ReviewRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminReviewServiceImpl implements AdminReviewService {

  private final ReviewRepository reviewRepository;
  private final ProductRepository productRepository;

  @Override
  @Transactional(readOnly = true)
  public Page<ReviewResponse> getAllReviews(int page, int size, String status) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

    if (status == null || status.isBlank() || "all".equalsIgnoreCase(status)) {
      return reviewRepository.findAll(pageable).map(this::toResponse);
    }

    if ("pending".equalsIgnoreCase(status)) {
      return reviewRepository
          .findByIsApprovedFalseOrderByCreatedAtDesc(pageable)
          .map(this::toResponse);
    }

    // approved
    List<Review> approved =
        reviewRepository.findAll(pageable).getContent().stream()
            .filter(r -> Boolean.TRUE.equals(r.getIsApproved()))
            .toList();
    return new PageImpl<>(
        approved.stream().map(this::toResponse).toList(), pageable, approved.size());
  }

  @Override
  @Transactional
  public ReviewResponse moderateReview(Long id, boolean approve) {
    Review r =
        reviewRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

    r.setIsApproved(approve);
    reviewRepository.save(r);

    // Recalculate product rating
    updateProductRating(r.getProduct());

    return toResponse(r);
  }

  @Override
  @Transactional
  public void deleteReview(Long id) {
    Review r =
        reviewRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
    Product product = r.getProduct();
    reviewRepository.delete(r);
    updateProductRating(product);
  }

  // ============ Helpers ============

  private void updateProductRating(Product product) {
    List<Review> approved =
        reviewRepository
            .findByProductIdAndIsApprovedTrueOrderByCreatedAtDesc(
                product.getId(), PageRequest.of(0, 10000))
            .getContent();

    if (approved.isEmpty()) {
      product.setRatingAvg(0.0);
      product.setRatingCount(0);
    } else {
      double avg = approved.stream().mapToInt(Review::getRating).average().orElse(0.0);
      product.setRatingAvg(Math.round(avg * 10.0) / 10.0);
      product.setRatingCount(approved.size());
    }
    productRepository.save(product);
  }

  private ReviewResponse toResponse(Review r) {
    return ReviewResponse.builder()
        .id(r.getId())
        .productId(r.getProduct().getId())
        .productTitle(r.getProduct().getTitle())
        .userId(r.getUser().getId())
        .userName(r.getUser().getName())
        .userInitials(getInitials(r.getUser().getName()))
        .rating(r.getRating())
        .title(r.getTitle())
        .comment(r.getComment())
        .isVerifiedPurchase(r.getIsVerifiedPurchase())
        .isApproved(r.getIsApproved())
        .createdAt(r.getCreatedAt())
        .build();
  }

  private String getInitials(String name) {
    if (name == null || name.isBlank()) return "?";
    String[] parts = name.trim().split("\\s+");
    if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
    return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
  }
}
