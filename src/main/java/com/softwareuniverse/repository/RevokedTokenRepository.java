package com.softwareuniverse.repository;

import com.softwareuniverse.entity.RevokedToken;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {

  boolean existsByToken(String token);

  void deleteByExpiresAtBefore(LocalDateTime time);
}
