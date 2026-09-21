package com.softwareuniverse.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GstReportResponse {
  private LocalDate fromDate;
  private LocalDate toDate;
  private long totalInvoices;
  private BigDecimal totalTaxableValue;
  private BigDecimal totalCgst;
  private BigDecimal totalSgst;
  private BigDecimal totalIgst;
  private BigDecimal totalTax;
  private BigDecimal totalInvoiceValue;
  private List<InvoiceLine> invoices;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class InvoiceLine {
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private String buyerName;
    private String buyerState;
    private BigDecimal taxableValue;
    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal igst;
    private BigDecimal total;
  }
}
