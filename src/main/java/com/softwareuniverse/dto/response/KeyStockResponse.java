package com.softwareuniverse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeyStockResponse {
  private Long productId;
  private String productTitle;
  private Long variantId;
  private String variantName;
  private long available;
  private long reserved;
  private long sold;
  private long revoked;
  private long total;
}
