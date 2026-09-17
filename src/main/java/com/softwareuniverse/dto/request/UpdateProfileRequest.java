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
}