package com.softwareuniverse.dto.request;

import lombok.Data;

@Data
public class KeyUpdateRequest {

  private String licenseKey;
  private String status;
  private Long productId;
  private Long variantId;
}
