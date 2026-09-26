package com.softwareuniverse.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewRequest {

  @NotNull(message = "Product ID is required")
  private Long productId;

  @NotNull(message = "Rating is required")
  @Min(1)
  @Max(5)
  private Integer rating;

  @NotBlank(message = "Title is required")
  @Size(max = 200, message = "Title must be 200 characters or less")
  private String title;

  private String comment;
}
