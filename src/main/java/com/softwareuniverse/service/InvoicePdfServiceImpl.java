package com.softwareuniverse.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.softwareuniverse.entity.Invoice;
import com.softwareuniverse.entity.OrderItem;
import com.softwareuniverse.repository.OrderItemRepository;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoicePdfServiceImpl implements InvoicePdfService {

  private final OrderItemRepository orderItemRepository;

  @Value("${app.uploads.dir:uploads}")
  private String uploadsDir;

  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
  private static final String GSTIN = "06ABCDE1234F1Z5";
  private static final String COMPANY_NAME = "Software Universe";
  private static final String COMPANY_ADDRESS =
      "330A, Durga Enclave, Gali No-7, Sehatpur, Faridabad, Haryana-121003";
  private static final String COMPANY_EMAIL = "support@softwareuniverse.in";
  private static final String COMPANY_PHONE = "+91 9911611207";

  @Override
  public String generateInvoicePdf(Invoice invoice) {
    try {
      Path invoiceDir = Paths.get(uploadsDir, "invoices").toAbsolutePath();
      Files.createDirectories(invoiceDir);

      String fileName = invoice.getInvoiceNumber().replace("/", "-") + ".pdf";
      File outFile = invoiceDir.resolve(fileName).toFile();

      Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
      PdfWriter.getInstance(doc, new FileOutputStream(outFile));
      doc.open();

      // --- Company Header ---
      Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD, new Color(15, 23, 42));
      Font smallFont = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.DARK_GRAY);
      Font boldFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK);
      Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);

      Paragraph title = new Paragraph(COMPANY_NAME, titleFont);
      title.setAlignment(Element.ALIGN_LEFT);
      doc.add(title);

      doc.add(new Paragraph(COMPANY_ADDRESS, smallFont));
      doc.add(new Paragraph("Email: " + COMPANY_EMAIL + " | Phone: " + COMPANY_PHONE, smallFont));
      doc.add(new Paragraph("GSTIN: " + GSTIN, smallFont));
      doc.add(Chunk.NEWLINE);

      // --- Invoice Title ---
      Font invoiceTitleFont = new Font(Font.HELVETICA, 16, Font.BOLD, new Color(37, 99, 235));
      Paragraph invTitle = new Paragraph("TAX INVOICE", invoiceTitleFont);
      invTitle.setAlignment(Element.ALIGN_RIGHT);
      doc.add(invTitle);

      // --- Invoice meta ---
      PdfPTable metaTable = new PdfPTable(2);
      metaTable.setWidthPercentage(100);
      metaTable.setSpacingBefore(10);
      metaTable.setSpacingAfter(10);

      metaTable.addCell(cell("Invoice No: " + invoice.getInvoiceNumber(), boldFont));
      metaTable.addCell(cell("Date: " + invoice.getGeneratedAt().format(DATE_FMT), normalFont));
      metaTable.addCell(cell("Order No: " + invoice.getOrder().getOrderNumber(), normalFont));
      metaTable.addCell(cell("Payment: PAID", boldFont));
      doc.add(metaTable);

      // --- Buyer Details ---
      Paragraph buyerHeading = new Paragraph("BILL TO", boldFont);
      buyerHeading.setSpacingBefore(10);
      doc.add(buyerHeading);
      doc.add(
          new Paragraph(invoice.getBuyerName() != null ? invoice.getBuyerName() : "-", normalFont));
      doc.add(
          new Paragraph(
              invoice.getBuyerEmail() != null ? invoice.getBuyerEmail() : "-", normalFont));
      if (invoice.getBuyerPhone() != null)
        doc.add(new Paragraph(invoice.getBuyerPhone(), normalFont));
      if (invoice.getBuyerAddress() != null)
        doc.add(new Paragraph(invoice.getBuyerAddress(), normalFont));
      if (invoice.getBuyerGstin() != null && !invoice.getBuyerGstin().isBlank())
        doc.add(new Paragraph("GSTIN: " + invoice.getBuyerGstin(), normalFont));
      doc.add(Chunk.NEWLINE);

      // --- Item Table ---
      PdfPTable itemTable = new PdfPTable(5);
      itemTable.setWidthPercentage(100);
      itemTable.setWidths(new float[] {5, 40, 15, 20, 20});

      itemTable.addCell(headerCell("#", boldFont));
      itemTable.addCell(headerCell("Description", boldFont));
      itemTable.addCell(headerCell("Qty", boldFont));
      itemTable.addCell(headerCell("Unit Price", boldFont));
      itemTable.addCell(headerCell("Amount", boldFont));

      var items = orderItemRepository.findByOrderId(invoice.getOrder().getId());
      int i = 1;
      for (OrderItem item : items) {
        String desc = item.getProductTitle();
        if (item.getVariantName() != null) desc += " (" + item.getVariantName() + ")";

        itemTable.addCell(cell(String.valueOf(i++), normalFont));
        itemTable.addCell(cell(desc, normalFont));
        itemTable.addCell(cell(String.valueOf(item.getQuantity()), normalFont));
        itemTable.addCell(cell("₹" + item.getUnitPrice(), normalFont));
        itemTable.addCell(cell("₹" + item.getLineTotal(), normalFont));
      }
      doc.add(itemTable);

      // --- Totals ---
      doc.add(Chunk.NEWLINE);
      PdfPTable totals = new PdfPTable(2);
      totals.setWidthPercentage(50);
      totals.setHorizontalAlignment(Element.ALIGN_RIGHT);

      addTotalRow(totals, "Subtotal", invoice.getSubtotal(), normalFont, boldFont);
      if (invoice.getDiscount() != null && invoice.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
        addTotalRow(totals, "Discount", invoice.getDiscount().negate(), normalFont, boldFont);
      }
      if (invoice.getCgst() != null && invoice.getCgst().compareTo(BigDecimal.ZERO) > 0) {
        addTotalRow(totals, "CGST (9%)", invoice.getCgst(), normalFont, boldFont);
        addTotalRow(totals, "SGST (9%)", invoice.getSgst(), normalFont, boldFont);
      }
      if (invoice.getIgst() != null && invoice.getIgst().compareTo(BigDecimal.ZERO) > 0) {
        addTotalRow(totals, "IGST (18%)", invoice.getIgst(), normalFont, boldFont);
      }
      addTotalRow(totals, "TOTAL", invoice.getTotal(), boldFont, boldFont);
      doc.add(totals);

      doc.add(Chunk.NEWLINE);
      doc.add(Chunk.NEWLINE);
      Paragraph footer =
          new Paragraph(
              "Thank you for your business! For support, contact "
                  + COMPANY_EMAIL
                  + " or WhatsApp "
                  + COMPANY_PHONE
                  + ".",
              smallFont);
      footer.setAlignment(Element.ALIGN_CENTER);
      doc.add(footer);

      doc.close();

      String relativePath = "/uploads/invoices/" + fileName;
      log.info("Invoice PDF generated: {}", relativePath);
      return relativePath;
    } catch (Exception e) {
      log.error("Failed to generate invoice PDF: {}", e.getMessage(), e);
      throw new RuntimeException("Invoice generation failed: " + e.getMessage());
    }
  }

  // ============== Helpers ==============

  private PdfPCell cell(String text, Font font) {
    PdfPCell c = new PdfPCell(new Phrase(text, font));
    c.setPadding(6);
    c.setBorderColor(new Color(220, 220, 220));
    return c;
  }

  private PdfPCell headerCell(String text, Font font) {
    PdfPCell c = new PdfPCell(new Phrase(text, font));
    c.setPadding(6);
    c.setBackgroundColor(new Color(240, 243, 250));
    c.setBorderColor(new Color(200, 210, 230));
    return c;
  }

  private void addTotalRow(
      PdfPTable t, String label, BigDecimal amount, Font labelFont, Font valueFont) {
    PdfPCell c1 = new PdfPCell(new Phrase(label, labelFont));
    c1.setBorder(Rectangle.NO_BORDER);
    c1.setPadding(4);
    c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
    PdfPCell c2 =
        new PdfPCell(new Phrase("₹" + (amount != null ? amount : BigDecimal.ZERO), valueFont));
    c2.setBorder(Rectangle.NO_BORDER);
    c2.setPadding(4);
    c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
    t.addCell(c1);
    t.addCell(c2);
  }
}
