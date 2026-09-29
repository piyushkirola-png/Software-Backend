package com.softwareuniverse.service;

import com.softwareuniverse.entity.Invoice;

public interface InvoicePdfService {
  String generateInvoicePdf(Invoice invoice);
}
