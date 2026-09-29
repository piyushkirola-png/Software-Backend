package com.softwareuniverse.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
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

  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern(
    "dd MMM yyyy"
  );

  private static final String GSTIN = "06ABCDE1234F1Z5";
  private static final String COMPANY_NAME = "Softora";
  private static final String COMPANY_ADDRESS_LINE_1 =
    "330A, Durga Enclave, Gali No-7";
  private static final String COMPANY_ADDRESS_LINE_2 = "Sehatpur, Faridabad";
  private static final String COMPANY_ADDRESS_LINE_3 = "Haryana-121003";
  private static final String COMPANY_EMAIL = "support@softora.in";
  private static final String COMPANY_PHONE = "+91 9911611207";
  private static final String ASSETS_DIR =
    "C:/Users/DELL/Documents/Software-Backend/src/main/resources/assets/";
  private static final String LOGO_PATH = ASSETS_DIR + "logo.png";
  private static final String SIGNATURE_PATH = ASSETS_DIR + "signature.png";
  private static final int MIN_TABLE_ROWS = 6;
  private static final float SIGNATURE_BOTTOM_OFFSET = 120f;
  private static final float THANKYOU_BOTTOM_OFFSET = 60f;

  @Override
  public String generateInvoicePdf(Invoice invoice) {
    try {
      Path invoiceDir = Paths.get(uploadsDir, "invoices").toAbsolutePath();
      Files.createDirectories(invoiceDir);

      String fileName = invoice.getInvoiceNumber().replace("/", "-") + ".pdf";
      File outFile = invoiceDir.resolve(fileName).toFile();

      Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
      PdfWriter writer = PdfWriter.getInstance(
        doc,
        new FileOutputStream(outFile)
      );
      doc.open();

      Font companyNameFont = new Font(
        Font.HELVETICA,
        20,
        Font.BOLD,
        new Color(15, 23, 42)
      );
      Font smallFont = new Font(
        Font.HELVETICA,
        9,
        Font.NORMAL,
        new Color(71, 85, 105)
      );
      Font invoiceTitleFont = new Font(
        Font.HELVETICA,
        26,
        Font.BOLD,
        new Color(15, 23, 42)
      );
      Font sectionHeadingFont = new Font(
        Font.HELVETICA,
        10,
        Font.BOLD,
        new Color(15, 23, 42)
      );
      Font normalFont = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);
      Font boldFont = new Font(Font.HELVETICA, 9, Font.BOLD, Color.BLACK);
      Font buyerFont = new Font(
        Font.HELVETICA,
        10,
        Font.NORMAL,
        new Color(30, 41, 59)
      );

      PdfPTable header = new PdfPTable(2);
      header.setWidthPercentage(100);
      header.setWidths(new float[] { 70, 30 });

      PdfPCell leftCell = new PdfPCell();
      leftCell.setBorder(Rectangle.NO_BORDER);
      leftCell.setPadding(0);

      Image logo = loadImageFromFile(LOGO_PATH);
      if (logo != null) {
        logo.scaleToFit(130, 60);
        logo.setAlignment(Image.ALIGN_LEFT);
        leftCell.addElement(logo);
        leftCell.addElement(new Paragraph(" ", smallFont));
      }

      Paragraph companyName = new Paragraph(COMPANY_NAME, companyNameFont);
      companyName.setSpacingAfter(4);
      leftCell.addElement(companyName);
      leftCell.addElement(new Paragraph(COMPANY_ADDRESS_LINE_1, smallFont));
      leftCell.addElement(new Paragraph(COMPANY_ADDRESS_LINE_2, smallFont));
      leftCell.addElement(new Paragraph(COMPANY_ADDRESS_LINE_3, smallFont));
      leftCell.addElement(new Paragraph(" ", smallFont));
      leftCell.addElement(new Paragraph("Email: " + COMPANY_EMAIL, smallFont));
      leftCell.addElement(new Paragraph("Phone: " + COMPANY_PHONE, smallFont));
      leftCell.addElement(new Paragraph("GSTIN: " + GSTIN, smallFont));

      header.addCell(leftCell);

      PdfPCell rightCell = new PdfPCell();
      rightCell.setBorder(Rectangle.NO_BORDER);
      rightCell.setPadding(0);
      rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
      rightCell.setVerticalAlignment(Element.ALIGN_TOP);

      Paragraph invTitle = new Paragraph("INVOICE", invoiceTitleFont);
      invTitle.setAlignment(Element.ALIGN_RIGHT);
      rightCell.addElement(invTitle);

      header.addCell(rightCell);

      doc.add(header);
      doc.add(Chunk.NEWLINE);

      PdfPTable metaTable = new PdfPTable(2);
      metaTable.setWidthPercentage(100);
      metaTable.setWidths(new float[] { 50, 50 });
      metaTable.setSpacingBefore(6);
      metaTable.setSpacingAfter(10);

      metaTable.addCell(
        metaCell("Invoice No: " + invoice.getInvoiceNumber(), boldFont)
      );
      metaTable.addCell(
        metaCell(
          "Date: " +
            (invoice.getGeneratedAt() != null
              ? invoice.getGeneratedAt().format(DATE_FMT)
              : "-"),
          normalFont
        )
      );
      metaTable.addCell(
        metaCell("Order No: " + invoice.getOrder().getOrderNumber(), normalFont)
      );
      metaTable.addCell(metaCell("Payment: PAID", boldFont));

      doc.add(metaTable);

      Paragraph billToHeading = new Paragraph("BILL TO", sectionHeadingFont);
      billToHeading.setSpacingBefore(4);
      billToHeading.setSpacingAfter(4);
      doc.add(billToHeading);

      doc.add(
        new Paragraph(
          invoice.getBuyerName() != null ? invoice.getBuyerName() : "-",
          buyerFont
        )
      );
      if (invoice.getBuyerEmail() != null) doc.add(
        new Paragraph(invoice.getBuyerEmail(), buyerFont)
      );
      if (invoice.getBuyerPhone() != null) doc.add(
        new Paragraph(invoice.getBuyerPhone(), buyerFont)
      );
      if (invoice.getBuyerAddress() != null) doc.add(
        new Paragraph(invoice.getBuyerAddress(), buyerFont)
      );
      if (
        invoice.getBuyerGstin() != null && !invoice.getBuyerGstin().isBlank()
      ) {
        doc.add(new Paragraph("GSTIN: " + invoice.getBuyerGstin(), buyerFont));
      }

      doc.add(Chunk.NEWLINE);

      PdfPTable itemTable = new PdfPTable(5);
      itemTable.setWidthPercentage(100);
      itemTable.setWidths(new float[] { 9, 41, 8, 21, 21 });

      itemTable.addCell(headerCell("S No", boldFont));
      itemTable.addCell(headerCell("Description", boldFont));
      itemTable.addCell(headerCell("Qty", boldFont));
      itemTable.addCell(headerCell("Unit Price", boldFont));
      itemTable.addCell(headerCell("Amount", boldFont));

      var items = orderItemRepository.findByOrderId(invoice.getOrder().getId());
      int i = 1;
      int realRowCount = 0;
      for (OrderItem item : items) {
        String desc = item.getProductTitle();
        if (item.getVariantName() != null) desc +=
          " (" + item.getVariantName() + ")";

        itemTable.addCell(bodyCell(String.valueOf(i++), normalFont));
        itemTable.addCell(bodyCell(desc, normalFont));
        itemTable.addCell(
          bodyCell(String.valueOf(item.getQuantity()), normalFont)
        );
        itemTable.addCell(bodyCell("₹" + item.getUnitPrice(), normalFont));
        itemTable.addCell(bodyCell("₹" + item.getLineTotal(), normalFont));
        realRowCount++;
      }

      int fillerRows = Math.max(0, MIN_TABLE_ROWS - realRowCount);
      for (int r = 0; r < fillerRows; r++) {
        itemTable.addCell(bodyCell(" ", normalFont));
        itemTable.addCell(bodyCell(" ", normalFont));
        itemTable.addCell(bodyCell(" ", normalFont));
        itemTable.addCell(bodyCell(" ", normalFont));
        itemTable.addCell(bodyCell(" ", normalFont));
      }

      doc.add(itemTable);
      doc.add(Chunk.NEWLINE);

      PdfPTable totals = new PdfPTable(2);
      totals.setWidthPercentage(50);
      totals.setHorizontalAlignment(Element.ALIGN_RIGHT);

      addTotalRow(
        totals,
        "Subtotal",
        invoice.getSubtotal(),
        normalFont,
        normalFont
      );
      if (
        invoice.getDiscount() != null &&
        invoice.getDiscount().compareTo(BigDecimal.ZERO) > 0
      ) {
        addTotalRow(
          totals,
          "Discount",
          invoice.getDiscount().negate(),
          normalFont,
          normalFont
        );
      }
      if (
        invoice.getCgst() != null &&
        invoice.getCgst().compareTo(BigDecimal.ZERO) > 0
      ) {
        addTotalRow(
          totals,
          "CGST (9%)",
          invoice.getCgst(),
          normalFont,
          normalFont
        );
        addTotalRow(
          totals,
          "SGST (9%)",
          invoice.getSgst(),
          normalFont,
          normalFont
        );
      }
      if (
        invoice.getIgst() != null &&
        invoice.getIgst().compareTo(BigDecimal.ZERO) > 0
      ) {
        addTotalRow(
          totals,
          "IGST (18%)",
          invoice.getIgst(),
          normalFont,
          normalFont
        );
      }
      addTotalRow(totals, "TOTAL", invoice.getTotal(), boldFont, boldFont);

      doc.add(totals);

      PdfContentByte cb = writer.getDirectContent();
      float pageWidth = doc.getPageSize().getWidth();
      float leftMargin = 40f;

      Image signature = loadImageFromFile(SIGNATURE_PATH);
      if (signature != null) {
        signature.scaleToFit(120, 50);
        signature.setAbsolutePosition(
          leftMargin,
          SIGNATURE_BOTTOM_OFFSET + 18f
        );
        cb.addImage(signature);
      }

      ColumnText.showTextAligned(
        cb,
        Element.ALIGN_LEFT,
        new Phrase("Authorized Signature", smallFont),
        leftMargin,
        SIGNATURE_BOTTOM_OFFSET,
        0
      );

      ColumnText.showTextAligned(
        cb,
        Element.ALIGN_CENTER,
        new Phrase(
          "Thank you for your business! For support, contact " + COMPANY_EMAIL,
          smallFont
        ),
        pageWidth / 2f,
        THANKYOU_BOTTOM_OFFSET,
        0
      );

      doc.close();

      String relativePath = "/uploads/invoices/" + fileName;
      log.info("Invoice PDF generated: {}", relativePath);
      return relativePath;
    } catch (Exception e) {
      log.error("Failed to generate invoice PDF: {}", e.getMessage(), e);
      throw new RuntimeException(
        "Invoice generation failed: " + e.getMessage()
      );
    }
  }

  private Image loadImageFromFile(String absolutePath) {
    try {
      File f = new File(absolutePath);
      if (!f.exists()) {
        log.warn("Image file NOT found at: {}", absolutePath);
        return null;
      }
      if (!f.canRead()) {
        log.warn("Image file NOT readable at: {}", absolutePath);
        return null;
      }
      log.info("Loading image: {} ({} bytes)", absolutePath, f.length());
      return Image.getInstance(f.getAbsolutePath());
    } catch (Exception e) {
      log.error(
        "FAILED to load image at {} — cause: {} ({})",
        absolutePath,
        e.getMessage(),
        e.getClass().getSimpleName(),
        e
      );
      return null;
    }
  }

  private PdfPCell metaCell(String text, Font font) {
    PdfPCell c = new PdfPCell(new Phrase(text, font));
    c.setPadding(4);
    c.setBorder(Rectangle.NO_BORDER);
    return c;
  }

  private PdfPCell bodyCell(String text, Font font) {
    PdfPCell c = new PdfPCell(new Phrase(text, font));
    c.setPadding(6);
    c.setBorderColor(new Color(226, 232, 240));
    c.setVerticalAlignment(Element.ALIGN_MIDDLE);
    return c;
  }

  private PdfPCell headerCell(String text, Font font) {
    PdfPCell c = new PdfPCell(new Phrase(text, font));
    c.setPadding(6);
    c.setBackgroundColor(new Color(241, 245, 249));
    c.setBorderColor(new Color(203, 213, 225));
    c.setVerticalAlignment(Element.ALIGN_MIDDLE);
    c.setHorizontalAlignment(Element.ALIGN_LEFT);
    return c;
  }

  private void addTotalRow(
    PdfPTable t,
    String label,
    BigDecimal amount,
    Font labelFont,
    Font valueFont
  ) {
    PdfPCell c1 = new PdfPCell(new Phrase(label, labelFont));
    c1.setBorder(Rectangle.NO_BORDER);
    c1.setPadding(5);
    c1.setHorizontalAlignment(Element.ALIGN_LEFT);

    PdfPCell c2 = new PdfPCell(
      new Phrase("₹" + (amount != null ? amount : BigDecimal.ZERO), valueFont)
    );
    c2.setBorder(Rectangle.NO_BORDER);
    c2.setPadding(5);
    c2.setHorizontalAlignment(Element.ALIGN_RIGHT);

    t.addCell(c1);
    t.addCell(c2);
  }
}
