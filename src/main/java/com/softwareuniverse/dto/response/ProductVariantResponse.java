package com.softwareuniverse.dto.response;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantResponse {

  private Long id;
  private String variantName;
  private BigDecimal mrp;
  private BigDecimal price;
  private Integer stockQuantity;
  private Long availableKeys;
  private Boolean isActive;
}
