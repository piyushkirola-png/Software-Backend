package com.softwareuniverse.controller;

import com.softwareuniverse.common.response.ApiResponse;
import com.softwareuniverse.dto.request.LoginRequest;
import com.softwareuniverse.dto.request.RegisterRequest;
import com.softwareuniverse.dto.request.ResetPasswordRequest;
import com.softwareuniverse.dto.request.SendOtpRequest;
import com.softwareuniverse.dto.request.VerifyOtpRequest;
import com.softwareuniverse.dto.response.AuthResponse;
import com.softwareuniverse.service.AuthService;
import com.softwareuniverse.service.OtpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final OtpService otpService;

  @PostMapping("/register")
  public ResponseEntity<ApiResponse<AuthResponse>> register(
      @Valid @RequestBody RegisterRequest request) {
    AuthResponse response = authService.register(request);
    return ResponseEntity.ok(ApiResponse.success("Registered successfully", response));
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
    AuthResponse response = authService.login(request);
    return ResponseEntity.ok(ApiResponse.success("Login successful", response));
  }

  @PostMapping("/otp/send")
  public ResponseEntity<ApiResponse<Void>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
    authService.sendPasswordResetOtp(request.getEmail());
    return ResponseEntity.ok(ApiResponse.success("OTP sent to your email", null));
  }

  @PostMapping("/otp/verify")
  public ResponseEntity<ApiResponse<Void>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
    otpService.verifyOtp(request.getEmail(), request.getCode());
    return ResponseEntity.ok(ApiResponse.success("OTP verified", null));
  }

  @PostMapping("/password/reset")
  public ResponseEntity<ApiResponse<Void>> resetPassword(
      @Valid @RequestBody ResetPasswordRequest request) {
    authService.resetPassword(request);
    return ResponseEntity.ok(ApiResponse.success("Password reset successful", null));
  }

  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    String token = null;
    if (header != null && header.startsWith("Bearer ")) {
      token = header.substring(7);
    }
    authService.logout(token);
    return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
  }
}
