package com.softwareuniverse.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CheckoutRequest {
  @NotNull(message = "Address ID is required")
  private Long addressId;

  private String couponCode;

  private String gateway;

  @Size(max = 20)
  private String gstNumber;

  @Size(max = 500)
  private String notes;
}