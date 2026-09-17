package com.softwareuniverse.service;

import com.softwareuniverse.dto.response.ReviewResponse;
import org.springframework.data.domain.Page;

public interface AdminReviewService {

  Page<ReviewResponse> getAllReviews(int page, int size, String status);

  ReviewResponse moderateReview(Long id, boolean approve);

  void deleteReview(Long id);
}