package com.softwareuniverse.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NewsletterRequest {

  @NotBlank @Email private String email;
}
