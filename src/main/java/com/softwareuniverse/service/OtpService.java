package com.softwareuniverse.service;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import com.softwareuniverse.entity.OtpCode;
import com.softwareuniverse.repository.OtpCodeRepository;
import java.time.LocalDateTime;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

  private final OtpCodeRepository otpCodeRepository;
  private final EmailService emailService;

  private static final int OTP_VALID_MINUTES = 10;

  @Transactional
  public void sendOtp(String email, String purpose) {
    // Delete any previous OTPs for this email
    otpCodeRepository.deleteByEmail(email);

    String code = String.format("%06d", new Random().nextInt(999999));

    OtpCode otp = new OtpCode();
    otp.setEmail(email);
    otp.setCode(code);
    otp.setPurpose(purpose);
    otp.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_VALID_MINUTES));
    otp.setUsed(false);
    otpCodeRepository.save(otp);

    emailService.sendOtp(email, code);
    log.info("OTP generated for {} — code: {} (dev mode)", email, code);
  }

  @Transactional(readOnly = true)
  public void verifyOtp(String email, String code) {
    OtpCode otp =
        otpCodeRepository
            .findTopByEmailAndCodeAndUsedFalseOrderByCreatedAtDesc(email, code)
            .orElseThrow(() -> new ResourceNotFoundException("Invalid OTP"));

    if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw new RuntimeException("OTP has expired");
    }
  }

  @Transactional
  public void markUsed(String email, String code) {
    otpCodeRepository
        .findTopByEmailAndCodeAndUsedFalseOrderByCreatedAtDesc(email, code)
        .ifPresent(
            otp -> {
              otp.setUsed(true);
              otpCodeRepository.save(otp);
            });
  }
}