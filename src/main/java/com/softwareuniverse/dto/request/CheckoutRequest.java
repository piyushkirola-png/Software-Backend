package com.softwareuniverse.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckoutRequest {

  @NotNull(message = "Address ID is required")
  private Long addressId;

  // Optional coupon code
  private String couponCode;

  // Optional — for gateway selection at checkout
  private String gateway; // RAZORPAY, CASHFREE, etc.
}