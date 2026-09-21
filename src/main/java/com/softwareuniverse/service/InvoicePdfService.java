package com.softwareuniverse.service;

import com.softwareuniverse.entity.Invoice;

public interface InvoicePdfService {

  /** Generate a PDF invoice, save it to disk, and return the relative path. */
  String generateInvoicePdf(Invoice invoice);
}
