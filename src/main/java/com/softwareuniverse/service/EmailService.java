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
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
      helper.setFrom(fromEmail);
      helper.setTo(to);
      helper.setSubject("Your verification code â€” Software Universe");

      String html = layout(
        "Verify your email",
        "<p style='margin:0 0 12px;color:#334155;font-size:15px;'>Hi there,</p>" +
          "<p style='margin:0 0 20px;color:#334155;font-size:15px;'>Use the code below to verify your email address. This code is valid for <b>5 minutes</b>.</p>" +
          "<div style='text-align:center;margin:28px 0;'>" +
          "<div style='display:inline-block;background:#F1F5F9;border:1px solid #E2E8F0;border-radius:12px;padding:18px 32px;font-family:monospace;font-size:32px;font-weight:700;letter-spacing:8px;color:#0B1F3A;'>" +
          code +
          "</div></div>" +
          "<p style='margin:20px 0 0;color:#64748B;font-size:13px;'>If you didn't request this code, you can safely ignore this email.</p>"
      );

      helper.setText(html, true);
      mailSender.send(message);
      log.info("OTP email sent to {}", to);
    } catch (Exception e) {
      log.error("Failed to send OTP email to {}: {}", to, e.getMessage());
    }
  }

  public void sendWelcome(String to, String name) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
      helper.setFrom(fromEmail);
      helper.setTo(to);
      helper.setSubject("Welcome to Software Universe, " + name + "! ðŸŽ‰");

      String body =
        "<p style='margin:0 0 12px;color:#334155;font-size:15px;'>Hi " +
        name +
        ",</p>" +
        "<p style='margin:0 0 24px;color:#334155;font-size:15px;'>Welcome aboard! Your account is verified and ready.</p>" +
        "<p style='margin:0 0 12px;color:#0B1F3A;font-size:14px;font-weight:700;text-transform:uppercase;letter-spacing:0.5px;'>Here's what you can do right now</p>" +
        "<table role='presentation' cellspacing='0' cellpadding='0' style='margin:0 0 28px;'>" +
        "<tr><td style='padding:6px 0;color:#16A34A;font-size:16px;width:24px;'>âœ“</td><td style='padding:6px 0;color:#334155;font-size:14px;'>Browse 60+ genuine software licenses</td></tr>" +
        "<tr><td style='padding:6px 0;color:#16A34A;font-size:16px;'>âœ“</td><td style='padding:6px 0;color:#334155;font-size:14px;'>Get instant email delivery with license keys</td></tr>" +
        "<tr><td style='padding:6px 0;color:#16A34A;font-size:16px;'>âœ“</td><td style='padding:6px 0;color:#334155;font-size:14px;'>Download GST invoices anytime</td></tr>" +
        "</table>" +
        "<div style='text-align:center;margin:32px 0 8px;'>" +
        "<a href='" +
        frontendUrl +
        "/products' style='display:inline-block;background:#2563EB;color:#FFFFFF;text-decoration:none;padding:14px 32px;border-radius:10px;font-weight:700;font-size:14px;'>Explore Products</a>" +
        "</div>";

      helper.setText(layout("Welcome to Software Universe!", body), true);
      mailSender.send(message);
      log.info("Welcome email sent to {}", to);
    } catch (Exception e) {
      log.error("Failed to send welcome email to {}: {}", to, e.getMessage());
    }
  }

  public void sendOrderConfirmation(
    Order order,
    List<LicenseKey> keys,
    Invoice invoice
  ) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setFrom(fromEmail);
      helper.setTo(order.getCustomerEmail());
      helper.setSubject("Order Confirmed â€” " + order.getOrderNumber());

      StringBuilder keysHtml = new StringBuilder();
      for (LicenseKey k : keys) {
        String title =
          k.getProduct().getTitle() +
          (k.getVariant() != null
            ? " (" + k.getVariant().getVariantName() + ")"
            : "");
        keysHtml
          .append(
            "<div style='border:1px solid #E2E8F0;border-radius:12px;padding:16px 18px;margin-bottom:12px;background:#FAFBFC;'>"
          )
          .append(
            "<div style='color:#0B1F3A;font-size:14px;font-weight:700;margin-bottom:8px;'>"
          )
          .append(escapeHtml(title))
          .append("</div>")
          .append(
            "<div style='color:#64748B;font-size:11px;text-transform:uppercase;letter-spacing:0.5px;margin-bottom:4px;'>License Key</div>"
          )
          .append(
            "<div style='font-family:monospace;font-size:15px;font-weight:700;color:#2563EB;word-break:break-all;'>"
          )
          .append(escapeHtml(k.getLicenseKey()))
          .append("</div>")
          .append("</div>");
      }

      String body =
        "<p style='margin:0 0 12px;color:#334155;font-size:15px;'>Hi " +
        escapeHtml(order.getUser().getName()) +
        ",</p>" +
        "<p style='margin:0 0 6px;color:#334155;font-size:15px;'>Thank you for your purchase! Your order has been confirmed.</p>" +
        "<p style='margin:0 0 24px;color:#64748B;font-size:13px;'>Order ID: <b style='color:#0B1F3A;'>" +
        escapeHtml(order.getOrderNumber()) +
        "</b></p>" +
        "<p style='margin:0 0 12px;color:#0B1F3A;font-size:14px;font-weight:700;text-transform:uppercase;letter-spacing:0.5px;'>Your License Keys</p>" +
        keysHtml +
        "<div style='background:#F1F5F9;border-radius:12px;padding:16px 18px;margin:20px 0;'>" +
        "<div style='color:#334155;font-size:13px;margin-bottom:6px;'>ðŸ’° <b>Total Paid:</b> <span style='color:#0B1F3A;font-size:15px;font-weight:700;'>â‚¹" +
        order.getTotal() +
        "</span></div>" +
        "<div style='color:#334155;font-size:13px;'>ðŸ“Ž Your GST invoice is attached with this email.</div>" +
        "</div>" +
        "<p style='margin:24px 0 0;padding-top:20px;border-top:1px solid #E2E8F0;color:#64748B;font-size:13px;'>Need help? Reply to this email or WhatsApp <b style='color:#0B1F3A;'>+91 9911611207</b>.</p>";

      helper.setText(layout("Order Confirmed", body), true);

      // Attach invoice PDF
      if (invoice != null && invoice.getPdfPath() != null) {
        String fileName = invoice
          .getPdfPath()
          .substring(invoice.getPdfPath().lastIndexOf("/") + 1);
        File pdfFile = Paths.get(uploadsDir, "invoices", fileName)
          .toAbsolutePath()
          .toFile();
        if (pdfFile.exists()) {
          helper.addAttachment(fileName, new FileSystemResource(pdfFile));
        }
      }

      mailSender.send(message);
      log.info("Order confirmation email sent to {}", order.getCustomerEmail());
    } catch (Exception e) {
      log.error(
        "Failed to send order confirmation email: {}",
        e.getMessage(),
        e
      );
    }
  }

  /** Wraps body content in a branded email layout. */
  private String layout(String title, String bodyContent) {
    return (
      "<!DOCTYPE html>" +
      "<html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1'></head>" +
      "<body style='margin:0;padding:0;background:#F1F5F9;font-family:-apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Arial,sans-serif;'>" +
      "<table role='presentation' width='100%' cellspacing='0' cellpadding='0' style='background:#F1F5F9;padding:32px 16px;'>" +
      "<tr><td align='center'>" +
      "<table role='presentation' width='100%' cellspacing='0' cellpadding='0' style='max-width:560px;background:#FFFFFF;border-radius:16px;overflow:hidden;box-shadow:0 4px 16px rgba(11,31,58,0.06);'>" +
      // Header bar
      "<tr><td style='background:#0B1F3A;padding:24px 32px;'>" +
      "<div style='color:#FFFFFF;font-size:18px;font-weight:800;letter-spacing:-0.3px;'>Software <span style='color:#60A5FA;'>Universe</span></div>" +
      "</td></tr>" +
      // Title
      "<tr><td style='padding:32px 32px 8px;'>" +
      "<h1 style='margin:0;color:#0B1F3A;font-size:22px;font-weight:800;letter-spacing:-0.3px;'>" +
      escapeHtml(title) +
      "</h1></td></tr>" +
      // Body
      "<tr><td style='padding:16px 32px 32px;line-height:1.6;'>" +
      bodyContent +
      "</td></tr>" +
      // Footer
      "<tr><td style='background:#F8FAFC;border-top:1px solid #E2E8F0;padding:20px 32px;text-align:center;'>" +
      "<div style='color:#64748B;font-size:12px;'>â€” Team Software Universe</div>" +
      "</td></tr>" +
      "</table>" +
      "<div style='color:#94A3B8;font-size:11px;margin-top:20px;'>Â© " +
      java.time.Year.now().getValue() +
      " Software Universe. All rights reserved.</div>" +
      "</td></tr></table></body></html>"
    );
  }

  private String escapeHtml(String s) {
    if (s == null) return "";
    return s
      .replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;");
  }
}
