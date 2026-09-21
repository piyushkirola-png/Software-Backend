package com.softwareuniverse.service;

import com.softwareuniverse.repository.InvoiceRepository;
import java.time.LocalDate;
import java.time.Month;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InvoiceNumberServiceImpl implements InvoiceNumberService {

  private final InvoiceRepository invoiceRepository;

  @Override
  @Transactional
  public synchronized String generateNextInvoiceNumber() {
    LocalDate today = LocalDate.now();
    // Indian FY: April 1 – March 31
    int fyStartYear =
        today.getMonthValue() >= Month.APRIL.getValue() ? today.getYear() : today.getYear() - 1;
    int fyEndYear = fyStartYear + 1;
    String fyCode = String.format("%d-%02d", fyStartYear, fyEndYear % 100); // e.g. 2026-27

    long count = invoiceRepository.count();
    long next = count + 1;
    String seq = String.format("%04d", next);

    return "SU/" + fyCode + "/" + seq;
  }
}
