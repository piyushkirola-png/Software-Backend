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
public class GstBreakdown {
  private BigDecimal taxableAmount;
  private BigDecimal cgst;
  private BigDecimal sgst;
  private BigDecimal igst;
  private BigDecimal totalTax;
  private boolean sameState;
}