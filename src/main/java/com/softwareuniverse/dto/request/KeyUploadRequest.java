package com.softwareuniverse.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class KeyUploadRequest {

  @NotNull(message = "Product ID is required")
  private Long productId;

  private Long variantId;

  @NotBlank(message = "License key is required")
  private String licenseKey;

  private String batchName;

  private String notes;
}
