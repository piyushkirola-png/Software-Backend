package com.softwareuniverse.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewRequest {

  @NotNull(message = "Product ID is required")
  private Long productId;

  @NotNull(message = "Rating is required")
  @Min(1)
  @Max(5)
  private Integer rating;

  private String title;

  private String comment;
}
