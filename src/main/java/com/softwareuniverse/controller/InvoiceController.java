package com.softwareuniverse.controller;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import com.softwareuniverse.common.response.ApiResponse;
import com.softwareuniverse.dto.response.InvoiceResponse;
import com.softwareuniverse.entity.Invoice;
import com.softwareuniverse.entity.User;
import com.softwareuniverse.repository.InvoiceRepository;
import com.softwareuniverse.repository.UserRepository;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

  private final InvoiceRepository invoiceRepository;
  private final UserRepository userRepository;

  /** Get invoice by order ID (user must own the order). */
  @GetMapping("/order/{orderId}")
  public ResponseEntity<ApiResponse<InvoiceResponse>> getByOrder(
      Principal principal, @PathVariable Long orderId) {
    Long userId = currentUserId(principal);
    Invoice invoice =
        invoiceRepository
            .findByOrderId(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice not found for this order"));

    if (!invoice.getUser().getId().equals(userId)) {
      throw new RuntimeException("Unauthorized");
    }
    return ResponseEntity.ok(ApiResponse.success("Invoice fetched", toResponse(invoice)));
  }

  /** Get invoice by invoice number. */
  @GetMapping("/{invoiceNumber}")
  public ResponseEntity<ApiResponse<InvoiceResponse>> getByNumber(
      Principal principal, @PathVariable String invoiceNumber) {
    Long userId = currentUserId(principal);
    Invoice invoice =
        invoiceRepository
            .findByInvoiceNumber(invoiceNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

    if (!invoice.getUser().getId().equals(userId)) {
      throw new RuntimeException("Unauthorized");
    }
    return ResponseEntity.ok(ApiResponse.success("Invoice fetched", toResponse(invoice)));
  }

  private Long currentUserId(Principal principal) {
    if (principal == null) {
      throw new ResourceNotFoundException("Unauthorized — please login");
    }
    User u =
        userRepository
            .findByEmail(principal.getName())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return u.getId();
  }

  private InvoiceResponse toResponse(Invoice inv) {
    return InvoiceResponse.builder()
        .id(inv.getId())
        .invoiceNumber(inv.getInvoiceNumber())
        .orderId(inv.getOrder().getId())
        .orderNumber(inv.getOrder().getOrderNumber())
        .subtotal(inv.getSubtotal())
        .discount(inv.getDiscount())
        .cgst(inv.getCgst())
        .sgst(inv.getSgst())
        .igst(inv.getIgst())
        .totalTax(inv.getTotalTax())
        .total(inv.getTotal())
        .buyerName(inv.getBuyerName())
        .buyerEmail(inv.getBuyerEmail())
        .buyerPhone(inv.getBuyerPhone())
        .buyerAddress(inv.getBuyerAddress())
        .buyerGstin(inv.getBuyerGstin())
        .buyerState(inv.getBuyerState())
        .pdfUrl(inv.getPdfPath())
        .generatedAt(inv.getGeneratedAt())
        .build();
  }
}