package com.softwareuniverse.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "full_name", nullable = false, length = 150)
  private String fullName;

  @Column(name = "phone", nullable = false, length = 20)
  private String phone;

  @Column(name = "address_line1", nullable = false, length = 255)
  private String addressLine1;

  @Column(name = "address_line2", length = 255)
  private String addressLine2;

  @Column(name = "city", nullable = false, length = 100)
  private String city;

  @Column(name = "state", nullable = false, length = 100)
  private String state;

  @Column(name = "pincode", nullable = false, length = 10)
  private String pincode;

  @Column(name = "country", nullable = false, length = 100)
  private String country = "India";

  @Column(name = "is_default", nullable = false)
  private Boolean isDefault = false;

  @Column(name = "gst_number", length = 20)
  private String gstNumber;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
    if (isDefault == null) isDefault = false;
    if (country == null) country = "India";
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
