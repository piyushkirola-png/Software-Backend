package com.softwareuniverse.service;

public interface InvoiceNumberService {
  /** Returns next invoice number in series: SU/2026-27/0001 */
  String generateNextInvoiceNumber();
}
