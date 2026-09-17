package com.softwareuniverse.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
  private Long id;
  private Long productId;
  private String productTitle;
  private Long userId;
  private String userName;
  private String userInitials;
  private Integer rating;
  private String title;
  private String comment;
  private Boolean isVerifiedPurchase;
  private Boolean isApproved;
  private LocalDateTime createdAt;
}