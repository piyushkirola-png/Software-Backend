package com.softwareuniverse.repository;

import com.softwareuniverse.entity.OtpCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

  Optional<OtpCode> findTopByEmailAndCodeAndUsedFalseOrderByCreatedAtDesc(
      String email, String code);

  void deleteByEmail(String email);
}