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
public class AddressResponse {

  private Long id;
  private String fullName;
  private String phone;
  private String addressLine1;
  private String addressLine2;
  private String city;
  private String state;
  private String pincode;
  private String country;
  private Boolean isDefault;
  private String gstNumber;
  private LocalDateTime createdAt;
}
