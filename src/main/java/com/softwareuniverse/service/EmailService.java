package com.softwareuniverse.service;

import com.softwareuniverse.entity.Invoice;
import com.softwareuniverse.entity.LicenseKey;
import com.softwareuniverse.entity.Order;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender mailSender;

  @Value("${spring.mail.username:no-reply@softwareuniverse.in}")
  private String fromEmail;

  @Value("${app.uploads.dir:uploads}")
  private String uploadsDir;

  @Value("${frontend.url:http://localhost:5100}")
  private String frontendUrl;

  public void sendOtp(String to, String code) {
    try {
      SimpleMailMessage m = new SimpleMailMessage();
      m.setFrom(fromEmail);
      m.setTo(to);
      m.setSubject("Password Reset OTP - Software Universe");
      m.setText(
          "Hello,\n\nYour OTP for password reset is: "
              + code
              + "\n\nValid for 10 minutes.\n\nSoftware Universe Team");
      mailSender.send(m);
      log.info("OTP email sent to {}", to);
    } catch (Exception e) {
      log.error("Failed to send OTP email to {}: {}", to, e.getMessage());
    }
  }

  public void sendWelcome(String to, String name) {
    try {
      SimpleMailMessage m = new SimpleMailMessage();
      m.setFrom(fromEmail);
      m.setTo(to);
      m.setSubject("Welcome to Software Universe!");
      m.setText("Hi " + name + ",\n\nWelcome to Software Universe.\n\nSoftware Universe Team");
      mailSender.send(m);
    } catch (Exception e) {
      log.error("Failed to send welcome email to {}: {}", to, e.getMessage());
    }
  }

  /** Order confirmation with license keys + invoice PDF attached. */
  public void sendOrderConfirmation(Order order, List<LicenseKey> keys, Invoice invoice) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setFrom(fromEmail);
      helper.setTo(order.getCustomerEmail());
      helper.setSubject("Order Confirmed — " + order.getOrderNumber() + " | Software Universe");

      StringBuilder keysHtml = new StringBuilder();
      for (LicenseKey k : keys) {
        String line =
            "<tr>"
                + "<td style='padding:8px;border:1px solid #e5e7eb;'>"
                + k.getProduct().getTitle()
                + (k.getVariant() != null ? " (" + k.getVariant().getVariantName() + ")" : "")
                + "</td>"
                + "<td style='padding:8px;border:1px solid #e5e7eb;font-family:monospace;font-weight:bold;color:#2563eb;'>"
                + k.getLicenseKey()
                + "</td>"
                + "</tr>";
        keysHtml.append(line);
      }

      String html =
          "<div style='font-family:Arial,sans-serif;color:#0f172a;max-width:640px;margin:0 auto;'>"
              + "<h2 style='color:#2563eb;'>Thank you for your order!</h2>"
              + "<p>Hi "
              + order.getUser().getName()
              + ",</p>"
              + "<p>Your order <b>"
              + order.getOrderNumber()
              + "</b> has been confirmed. Below are your license keys:</p>"
              + "<table style='border-collapse:collapse;width:100%;margin:16px 0;'>"
              + "<thead><tr>"
              + "<th style='padding:8px;border:1px solid #e5e7eb;background:#f8fafc;text-align:left;'>Product</th>"
              + "<th style='padding:8px;border:1px solid #e5e7eb;background:#f8fafc;text-align:left;'>License Key</th>"
              + "</tr></thead>"
              + "<tbody>"
              + keysHtml
              + "</tbody></table>"
              + "<p><b>Total Paid:</b> ₹"
              + order.getTotal()
              + "</p>"
              + "<p>Your GST invoice is attached with this email.</p>"
              + "<p>You can also view your orders and download files from your dashboard: "
              + "<a href='"
              + frontendUrl
              + "/my-account/orders'>"
              + frontendUrl
              + "/my-account/orders</a></p>"
              + "<p style='color:#64748b;font-size:12px;'>For support, reply to this email or WhatsApp +91 9911611207.</p>"
              + "<p>— Software Universe Team</p>"
              + "</div>";

      helper.setText(html, true);

      // Attach invoice PDF
      if (invoice != null && invoice.getPdfPath() != null) {
        String fileName = invoice.getPdfPath().substring(invoice.getPdfPath().lastIndexOf("/") + 1);
        File pdfFile = Paths.get(uploadsDir, "invoices", fileName).toAbsolutePath().toFile();
        if (pdfFile.exists()) {
          helper.addAttachment(fileName, new FileSystemResource(pdfFile));
        }
      }

      mailSender.send(message);
      log.info("Order confirmation email sent to {}", order.getCustomerEmail());
    } catch (Exception e) {
      log.error("Failed to send order confirmation email: {}", e.getMessage(), e);
    }
  }
}
