package com.softwareuniverse.repository;

import com.softwareuniverse.entity.Invoice;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
  Optional<Invoice> findByOrderId(Long orderId);

  Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
}
