package com.softwareuniverse.service;

import com.softwareuniverse.dto.request.ReviewRequest;
import com.softwareuniverse.dto.response.ReviewResponse;
import java.util.List;
import org.springframework.data.domain.Page;

public interface ReviewService {
  Page<ReviewResponse> getApprovedReviews(Long productId, int page, int size);

  List<ReviewResponse> getFeaturedReviews();

  ReviewResponse createReview(Long userId, ReviewRequest request);

  List<ReviewResponse> getMyReviews(Long userId);
}
