package com.softwareuniverse.dto.request;

import lombok.Data;

@Data
public class AdminUserUpdateRequest {
  private String name;
  private String phone;
  private String role; // USER or ADMIN
  private Boolean isActive;
}
