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
public class KeyResponse {

  private Long id;
  private Long productId;
  private String productTitle;
  private String productSlug;
  private String productThumbnailUrl;
  private String productDownloadUrl;
  private Long variantId;
  private String variantName;
  private String licenseKey;
  private String status;
  private Long orderId;
  private String orderNumber;
  private LocalDateTime soldAt;
}
