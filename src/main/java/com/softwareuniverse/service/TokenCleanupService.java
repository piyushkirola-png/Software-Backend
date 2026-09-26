package com.softwareuniverse.service;

import com.softwareuniverse.repository.RevokedTokenRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenCleanupService {

  private final RevokedTokenRepository revokedTokenRepository;

  @Scheduled(fixedRate = 60 * 60 * 1000) // every hour
  @Transactional
  public void cleanupExpiredTokens() {
    try {
      revokedTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
      log.debug("Cleaned up expired revoked tokens");
    } catch (Exception e) {
      log.warn("Revoked-token cleanup failed: {}", e.getMessage());
    }
  }
}
