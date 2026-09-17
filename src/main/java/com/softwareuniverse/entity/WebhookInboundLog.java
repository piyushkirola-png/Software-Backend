package com.softwareuniverse.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "webhook_inbound_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WebhookInboundLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "gateway", nullable = false, length = 50)
  private String gateway; // RAZORPAY, CASHFREE, etc.

  @Column(name = "event_type", length = 100)
  private String eventType;

  @Column(name = "payload", columnDefinition = "TEXT")
  private String payload;

  @Column(name = "signature", length = 500)
  private String signature;

  @Column(name = "verified", nullable = false)
  private Boolean verified = false;

  @Column(name = "processed", nullable = false)
  private Boolean processed = false;

  @Column(name = "error_message", length = 500)
  private String errorMessage;

  @Column(name = "received_at")
  private LocalDateTime receivedAt;

  @PrePersist
  protected void onCreate() {
    receivedAt = LocalDateTime.now();
    if (verified == null) verified = false;
    if (processed == null) processed = false;
  }
}