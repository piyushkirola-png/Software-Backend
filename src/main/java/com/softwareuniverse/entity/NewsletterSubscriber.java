package com.softwareuniverse.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "newsletter_subscribers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewsletterSubscriber {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 150)
  private String email;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  @Column(name = "subscribed_at")
  private LocalDateTime subscribedAt;

  @PrePersist
  protected void onCreate() {
    subscribedAt = LocalDateTime.now();
    if (isActive == null) isActive = true;
  }
}