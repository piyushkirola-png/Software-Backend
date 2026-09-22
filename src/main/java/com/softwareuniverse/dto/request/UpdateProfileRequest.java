package com.softwareuniverse.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

  @Size(min = 2, max = 150)
  private String name;

  @Size(max = 20)
  private String phone;

  private String avatarUrl;

  private String gender;
  private String currentAddress;
  private String city;
  private String state;
  private String country;
  private String pincode;
}
