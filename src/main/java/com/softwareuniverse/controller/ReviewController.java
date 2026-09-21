package com.softwareuniverse.controller;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import com.softwareuniverse.common.response.ApiResponse;
import com.softwareuniverse.dto.request.ReviewRequest;
import com.softwareuniverse.dto.response.ReviewResponse;
import com.softwareuniverse.entity.User;
import com.softwareuniverse.repository.UserRepository;
import com.softwareuniverse.service.ReviewService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

  private final ReviewService reviewService;
  private final UserRepository userRepository;

  /** Public — approved reviews for a product. */
  @GetMapping("/product/{productId}")
  public ResponseEntity<ApiResponse<Page<ReviewResponse>>> forProduct(
      @PathVariable Long productId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(
        ApiResponse.success(
            "Reviews fetched", reviewService.getApprovedReviews(productId, page, size)));
  }

  /** Public — for home page carousel. */
  @GetMapping("/featured")
  public ResponseEntity<ApiResponse<List<ReviewResponse>>> featured() {
    return ResponseEntity.ok(
        ApiResponse.success("Featured reviews", reviewService.getFeaturedReviews()));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<ReviewResponse>> create(
      Principal principal, @Valid @RequestBody ReviewRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            "Review submitted", reviewService.createReview(currentUserId(principal), request)));
  }

  @GetMapping("/my")
  public ResponseEntity<ApiResponse<List<ReviewResponse>>> myReviews(Principal principal) {
    return ResponseEntity.ok(
        ApiResponse.success("My reviews", reviewService.getMyReviews(currentUserId(principal))));
  }

  private Long currentUserId(Principal principal) {
    if (principal == null) throw new ResourceNotFoundException("Unauthorized");
    User user =
        userRepository
            .findByEmail(principal.getName())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return user.getId();
  }
}
