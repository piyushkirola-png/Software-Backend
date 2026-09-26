package com.softwareuniverse.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

  private Long id;
  private String name;
  private String email;
  private String phone;
  private String avatarUrl;
  private String role;
  private Boolean isActive;
  private String gender;
  private String currentAddress;
  private String city;
  private String state;
  private String country;
  private String pincode;
  private LocalDateTime createdAt;
}
