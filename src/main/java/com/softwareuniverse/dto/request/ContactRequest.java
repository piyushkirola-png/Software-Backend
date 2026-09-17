package com.softwareuniverse.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContactRequest {

  @NotBlank(message = "Name is required")
  private String name;

  @NotBlank
  @Email
  private String email;

  private String phone;

  @NotBlank(message = "Message is required")
  private String message;
}