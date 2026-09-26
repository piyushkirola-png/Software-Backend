package com.softwareuniverse.config;

import com.softwareuniverse.entity.Role;
import com.softwareuniverse.entity.User;
import com.softwareuniverse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  private static final String ADMIN_NAME = "Admin";
  private static final String ADMIN_EMAIL = "admin@softwareuniverse.in";
  private static final String ADMIN_PASSWORD = "Admin@123";
  private static final String ADMIN_PHONE = "+919876543210";

  @Override
  public void run(String... args) {
    seedAdmin();
  }

  private void seedAdmin() {
    log.info("🔍 DataSeeder: checking admin '{}'", ADMIN_EMAIL);

    if (userRepository.existsByEmail(ADMIN_EMAIL)) {
      log.info("✅ Admin already exists — skipping");
      return;
    }

    User admin = new User();
    admin.setName(ADMIN_NAME);
    admin.setEmail(ADMIN_EMAIL);
    admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
    admin.setPhone(ADMIN_PHONE);
    admin.setRole(Role.ADMIN);
    admin.setIsActive(true);
    userRepository.save(admin);

    log.info(
      "🌱 Admin created — email: {} | password: {}",
      ADMIN_EMAIL,
      ADMIN_PASSWORD
    );
  }
}
