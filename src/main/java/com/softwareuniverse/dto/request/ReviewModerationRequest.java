package com.softwareuniverse.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewModerationRequest {

  @NotNull(message = "Approve flag is required")
  private Boolean approve;
}
