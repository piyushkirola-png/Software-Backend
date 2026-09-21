package com.softwareuniverse.controller;

import com.softwareuniverse.common.response.ApiResponse;
import com.softwareuniverse.dto.response.ReviewResponse;
import com.softwareuniverse.service.AdminReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReviewController {

  private final AdminReviewService adminReviewService;

  @GetMapping
  public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getAll(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String status) {
    return ResponseEntity.ok(
        ApiResponse.success(
            "Reviews fetched", adminReviewService.getAllReviews(page, size, status)));
  }

  @PostMapping("/{id}/approve")
  public ResponseEntity<ApiResponse<ReviewResponse>> approve(@PathVariable Long id) {
    return ResponseEntity.ok(
        ApiResponse.success("Review approved", adminReviewService.moderateReview(id, true)));
  }

  @PostMapping("/{id}/reject")
  public ResponseEntity<ApiResponse<ReviewResponse>> reject(@PathVariable Long id) {
    return ResponseEntity.ok(
        ApiResponse.success("Review rejected", adminReviewService.moderateReview(id, false)));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
    adminReviewService.deleteReview(id);
    return ResponseEntity.ok(ApiResponse.success("Review deleted", null));
  }
}
