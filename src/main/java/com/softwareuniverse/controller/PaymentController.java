package com.softwareuniverse.controller;

import com.softwareuniverse.common.exception.ResourceNotFoundException;

import com.softwareuniverse.common.response.ApiResponse;

import com.softwareuniverse.dto.request.PaymentRequest;

import com.softwareuniverse.dto.response.PaymentInitiateResponse;

import com.softwareuniverse.dto.response.PaymentResponse;

import com.softwareuniverse.entity.User;

import com.softwareuniverse.repository.UserRepository;

import com.softwareuniverse.service.PaymentService;

import jakarta.validation.Valid;

import java.security.Principal;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController

@RequestMapping("/api/payments")

@RequiredArgsConstructor

public class PaymentController {

  private final PaymentService paymentService;

  private final UserRepository userRepository;

  @PostMapping("/initiate")

  public ResponseEntity<ApiResponse<PaymentInitiateResponse>> initiate(Principal principal, @Valid @RequestBody PaymentRequest request) {

    return ResponseEntity.ok(ApiResponse.success("Payment initiated", paymentService.initiatePayment(currentUserId(principal), request)));

  }

  @GetMapping("/{paymentId}")

  public ResponseEntity<ApiResponse<PaymentResponse>> getById(Principal principal, @PathVariable Long paymentId) {

    return ResponseEntity.ok(ApiResponse.success("Payment fetched", paymentService.getPaymentById(currentUserId(principal), paymentId)));

  }

  @GetMapping("/order/{orderId}")

  public ResponseEntity<ApiResponse<List<PaymentResponse>>> forOrder(Principal principal, @PathVariable Long orderId) {

    return ResponseEntity.ok(ApiResponse.success("Payments fetched", paymentService.getPaymentsForOrder(currentUserId(principal), orderId)));

  }

  @PostMapping("/{paymentId}/simulate-success")

  public ResponseEntity<ApiResponse<PaymentResponse>> simulate(Principal principal, @PathVariable Long paymentId) {

    return ResponseEntity.ok(ApiResponse.success("Payment simulated", paymentService.simulateSuccess(currentUserId(principal), paymentId)));

  }

  private Long currentUserId(Principal principal) {

    if (principal == null) throw new ResourceNotFoundException("Unauthorized");

    User user = userRepository.findByEmail(principal.getName()).orElseThrow(() -> new ResourceNotFoundException("User not found"));

    return user.getId();

  }

}