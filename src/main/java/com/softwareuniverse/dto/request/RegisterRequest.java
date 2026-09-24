package com.softwareuniverse.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

  @NotBlank(message = "Name is required")
  @Size(min = 2, max = 150)
  private String name;

  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email")
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 6, max = 100, message = "Password must be 6-100 characters")
  private String password;

  @NotBlank(message = "Phone number is required")
  @Pattern(
    regexp = "^([+]?91[\\s-]?)?[6-9]\\d{9}$",
    message = "Enter a valid 10-digit Indian mobile number"
  )
  private String phone;
}
