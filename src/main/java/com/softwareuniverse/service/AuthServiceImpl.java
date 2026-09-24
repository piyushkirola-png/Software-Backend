package com.softwareuniverse.service;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import com.softwareuniverse.dto.request.LoginRequest;
import com.softwareuniverse.dto.request.RegisterRequest;
import com.softwareuniverse.dto.request.ResetPasswordRequest;
import com.softwareuniverse.dto.response.AuthResponse;
import com.softwareuniverse.entity.RevokedToken;
import com.softwareuniverse.entity.Role;
import com.softwareuniverse.entity.User;
import com.softwareuniverse.repository.RevokedTokenRepository;
import com.softwareuniverse.repository.UserRepository;
import com.softwareuniverse.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final RevokedTokenRepository revokedTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider tokenProvider;
  private final AuthenticationManager authenticationManager;
  private final OtpService otpService;
  private final EmailService emailService;

  @Override
  @Transactional
  public void register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new RuntimeException("Email already registered");
    }

    User user = new User();
    user.setName(request.getName());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setPhone(normalizePhone(request.getPhone()));
    user.setRole(Role.USER);
    user.setIsActive(true);
    user.setIsVerified(false);
    userRepository.save(user);

    // Send OTP — welcome email goes out AFTER verification
    otpService.sendOtp(user.getEmail(), "SIGNUP");
  }

  @Override
  @Transactional
  public AuthResponse verifySignupOtp(String email, String code) {
    User user = userRepository
      .findByEmail(email)
      .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (Boolean.TRUE.equals(user.getIsVerified())) {
      String token = tokenProvider.createToken(
        user.getEmail(),
        user.getRole().name()
      );
      return buildAuthResponse(user, token);
    }

    otpService.verifyOtp(email, code, "SIGNUP");
    otpService.markUsed(email, code);

    user.setIsVerified(true);
    userRepository.save(user);

    try {
      emailService.sendWelcome(user.getEmail(), user.getName());
    } catch (Exception e) {
      log.warn("Failed to send welcome email to {}: {}", email, e.getMessage());
    }

    String token = tokenProvider.createToken(
      user.getEmail(),
      user.getRole().name()
    );
    return buildAuthResponse(user, token);
  }

  private String normalizePhone(String raw) {
    if (raw == null || raw.isBlank()) return null;
    String digits = raw.replaceAll("[^0-9]", "");
    if (digits.startsWith("91") && digits.length() == 12) {
      digits = digits.substring(2);
    } else if (digits.startsWith("0") && digits.length() == 11) {
      digits = digits.substring(1);
    }
    if (digits.length() != 10) return raw.trim();
    return "+91 " + digits;
  }

  @Override
  public AuthResponse login(LoginRequest request) {
    try {
      authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
          request.getEmail(),
          request.getPassword()
        )
      );
    } catch (Exception e) {
      throw new BadCredentialsException("Invalid email or password");
    }

    User user = userRepository
      .findByEmail(request.getEmail())
      .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (Boolean.FALSE.equals(user.getIsVerified())) {
      try {
        otpService.sendOtp(user.getEmail(), "SIGNUP");
      } catch (Exception ignored) {}
      throw new RuntimeException(
        "Email not verified. We have sent a new OTP to your email."
      );
    }

    String token = tokenProvider.createToken(
      user.getEmail(),
      user.getRole().name()
    );
    return buildAuthResponse(user, token);
  }

  @Override
  public void sendPasswordResetOtp(String email) {
    if (!userRepository.existsByEmail(email)) {
      throw new ResourceNotFoundException("No account found with this email");
    }
    otpService.sendOtp(email, "FORGOT_PASSWORD");
  }

  @Override
  @Transactional
  public void resetPassword(ResetPasswordRequest request) {
    otpService.verifyOtp(request.getEmail(), request.getCode());

    User user = userRepository
      .findByEmail(request.getEmail())
      .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);

    otpService.markUsed(request.getEmail(), request.getCode());
    log.info("Password reset successful for {}", request.getEmail());
  }

  @Override
  @Transactional
  public void logout(String token) {
    if (token == null || token.isEmpty()) return;
    if (revokedTokenRepository.existsByToken(token)) return;

    RevokedToken revoked = new RevokedToken();
    revoked.setToken(token);
    revoked.setExpiresAt(tokenProvider.getExpiryFromToken(token));
    revokedTokenRepository.save(revoked);
  }

  private AuthResponse buildAuthResponse(User user, String token) {
    return AuthResponse.builder()
      .token(token)
      .tokenType("Bearer")
      .userId(user.getId())
      .name(user.getName())
      .email(user.getEmail())
      .role(user.getRole().name())
      .phone(user.getPhone())
      .avatarUrl(user.getAvatarUrl())
      .build();
  }
}
