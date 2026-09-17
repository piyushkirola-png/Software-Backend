package com.softwareuniverse.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressRequest {

  @NotBlank(message = "Full name is required")
  @Size(max = 150)
  private String fullName;

  @NotBlank(message = "Phone is required")
  @Size(max = 20)
  private String phone;

  @NotBlank(message = "Address line 1 is required")
  @Size(max = 255)
  private String addressLine1;

  @Size(max = 255)
  private String addressLine2;

  @NotBlank(message = "City is required")
  @Size(max = 100)
  private String city;

  @NotBlank(message = "State is required")
  @Size(max = 100)
  private String state;

  @NotBlank(message = "Pincode is required")
  @Size(max = 10)
  private String pincode;

  @Size(max = 100)
  private String country = "India";

  private Boolean isDefault = false;
}