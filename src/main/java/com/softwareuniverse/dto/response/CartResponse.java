package com.softwareuniverse.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
  private Long id;
  private List<CartItemResponse> items;
  private Integer totalItems;
  private BigDecimal subtotal;
}