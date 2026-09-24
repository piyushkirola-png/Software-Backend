package com.softwareuniverse.service;

import com.softwareuniverse.dto.request.LoginRequest;
import com.softwareuniverse.dto.request.RegisterRequest;
import com.softwareuniverse.dto.request.ResetPasswordRequest;
import com.softwareuniverse.dto.response.AuthResponse;

public interface AuthService {
  void register(RegisterRequest request);

  AuthResponse verifySignupOtp(String email, String code);

  AuthResponse login(LoginRequest request);

  void sendPasswordResetOtp(String email);

  void resetPassword(ResetPasswordRequest request);

  void logout(String token);
}
