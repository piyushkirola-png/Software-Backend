package com.softwareuniverse.service;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import com.softwareuniverse.dto.request.ReviewRequest;
import com.softwareuniverse.dto.response.ReviewResponse;
import com.softwareuniverse.entity.*;
import com.softwareuniverse.repository.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

  private final ReviewRepository reviewRepository;
  private final ProductRepository productRepository;
  private final UserRepository userRepository;
  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;

  @Override
  @Transactional(readOnly = true)
  public Page<ReviewResponse> getApprovedReviews(Long productId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    return reviewRepository
        .findByProductIdAndIsApprovedTrueOrderByCreatedAtDesc(productId, pageable)
        .map(this::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReviewResponse> getFeaturedReviews() {
    return reviewRepository
        .findTop10ByIsApprovedTrueAndIsVerifiedPurchaseTrueOrderByCreatedAtDesc()
        .stream()
        .map(this::toResponse)
        .toList();
  }

  @Override
  @Transactional
  public ReviewResponse createReview(Long userId, ReviewRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    Product product =
        productRepository
            .findById(request.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

    if (reviewRepository.existsByProductIdAndUserId(product.getId(), userId)) {
      throw new RuntimeException("You have already reviewed this product");
    }

    // Verify purchase: user must have a SUCCESS order containing this product
    Order verifiedOrder = findVerifiedOrder(userId, product.getId());

    Review review = new Review();
    review.setProduct(product);
    review.setUser(user);
    review.setOrder(verifiedOrder);
    review.setRating(request.getRating());
    review.setTitle(request.getTitle());
    review.setComment(request.getComment());
    review.setIsVerifiedPurchase(verifiedOrder != null);
    review.setIsApproved(false); // admin approval required

    reviewRepository.save(review);

    // Recalculate product rating (only approved reviews count — but this updates later on approval)
    updateProductRating(product);

    return toResponse(review);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReviewResponse> getMyReviews(Long userId) {
    return reviewRepository.findAll().stream()
        .filter(r -> r.getUser().getId().equals(userId))
        .map(this::toResponse)
        .toList();
  }

  // ============ Helpers ============

  private Order findVerifiedOrder(Long userId, Long productId) {
    List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    for (Order o : orders) {
      if (o.getStatus() != OrderStatus.SUCCESS) continue;
      boolean hasProduct =
          orderItemRepository.findByOrderId(o.getId()).stream()
              .anyMatch(item -> item.getProduct().getId().equals(productId));
      if (hasProduct) return o;
    }
    return null;
  }

  private void updateProductRating(Product product) {
    // Simple: only approved reviews count
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
    String initials = getInitials(r.getUser().getName());
    return ReviewResponse.builder()
        .id(r.getId())
        .productId(r.getProduct().getId())
        .productTitle(r.getProduct().getTitle())
        .userId(r.getUser().getId())
        .userName(r.getUser().getName())
        .userInitials(initials)
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
