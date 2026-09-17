package com.softwareuniverse.service;

import com.softwareuniverse.dto.response.GstBreakdown;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GstCalculatorService {

  /** Seller's state — used to decide CGST/SGST vs IGST. */
  @Value("${app.seller.state:Haryana}")
  private String sellerState;

  @Value("${app.gst.rate:0.18}")
  private BigDecimal gstRate;

  /**
   * Compute GST breakdown.
   *
   * <p>Same state → CGST + SGST (each half) Different state → IGST (full)
   */
  public GstBreakdown calculate(BigDecimal taxableAmount, String buyerState) {
    if (taxableAmount == null) taxableAmount = BigDecimal.ZERO;

    BigDecimal totalTax =
        taxableAmount.multiply(gstRate).setScale(2, RoundingMode.HALF_UP);

    boolean sameState =
        buyerState != null
            && sellerState != null
            && buyerState.trim().equalsIgnoreCase(sellerState.trim());

    if (sameState) {
      BigDecimal half = totalTax.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
      return GstBreakdown.builder()
          .taxableAmount(taxableAmount)
          .cgst(half)
          .sgst(half)
          .igst(BigDecimal.ZERO)
          .totalTax(totalTax)
          .sameState(true)
          .build();
    } else {
      return GstBreakdown.builder()
          .taxableAmount(taxableAmount)
          .cgst(BigDecimal.ZERO)
          .sgst(BigDecimal.ZERO)
          .igst(totalTax)
          .totalTax(totalTax)
          .sameState(false)
          .build();
    }
  }
}