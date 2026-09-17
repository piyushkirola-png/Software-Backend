package com.softwareuniverse.service;

import com.softwareuniverse.dto.request.ContactRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

  private final JavaMailSender mailSender;

  @Value("${spring.mail.username:no-reply@softwareuniverse.in}")
  private String fromEmail;

  @Value("${app.contact.recipient:support@softwareuniverse.in}")
  private String contactRecipient;

  @Override
  public void submit(ContactRequest request) {
    try {
      SimpleMailMessage m = new SimpleMailMessage();
      m.setFrom(fromEmail);
      m.setTo(contactRecipient);
      m.setReplyTo(request.getEmail());
      m.setSubject("New Contact Message from " + request.getName());
      m.setText(
          "Name: "
              + request.getName()
              + "\nEmail: "
              + request.getEmail()
              + "\nPhone: "
              + (request.getPhone() != null ? request.getPhone() : "-")
              + "\n\nMessage:\n"
              + request.getMessage());
      mailSender.send(m);
      log.info("Contact form message sent from {}", request.getEmail());
    } catch (Exception e) {
      log.error("Failed to send contact email: {}", e.getMessage());
    }
  }
}