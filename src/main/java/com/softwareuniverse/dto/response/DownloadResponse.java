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
public class DownloadResponse {

  private Long productId;
  private String productTitle;
  private String slug;
  private String thumbnailUrl;
  private String downloadUrl;
  private LocalDateTime purchasedAt;
}
