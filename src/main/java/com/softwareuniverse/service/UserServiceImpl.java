package com.softwareuniverse.service;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import com.softwareuniverse.dto.request.ChangePasswordRequest;
import com.softwareuniverse.dto.request.UpdateProfileRequest;
import com.softwareuniverse.dto.response.UserResponse;
import com.softwareuniverse.entity.Gender;
import com.softwareuniverse.entity.User;
import com.softwareuniverse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional(readOnly = true)
  public UserResponse getProfile(Long userId) {
    User user = userRepository
      .findById(userId)
      .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return toResponse(user);
  }

  @Override
  @Transactional
  public UserResponse updateProfile(Long userId, UpdateProfileRequest req) {
    User user = userRepository
      .findById(userId)
      .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (req.getName() != null && !req.getName().isBlank()) user.setName(
      req.getName().trim()
    );

    if (req.getPhone() != null) user.setPhone(blankToNull(req.getPhone()));

    if (req.getAvatarUrl() != null) user.setAvatarUrl(req.getAvatarUrl());

    if (req.getGender() != null && !req.getGender().isBlank()) {
      try {
        user.setGender(Gender.valueOf(req.getGender().toUpperCase()));
      } catch (IllegalArgumentException e) {
        throw new RuntimeException("Invalid gender value");
      }
    }

    if (req.getCurrentAddress() != null) user.setCurrentAddress(
      blankToNull(req.getCurrentAddress())
    );

    if (req.getCity() != null) user.setCity(blankToNull(req.getCity()));
    if (req.getState() != null) user.setState(blankToNull(req.getState()));
    if (req.getCountry() != null) user.setCountry(
      blankToNull(req.getCountry())
    );
    if (req.getPincode() != null) user.setPincode(
      blankToNull(req.getPincode())
    );

    userRepository.save(user);
    return toResponse(user);
  }

  @Override
  @Transactional
  public void changePassword(Long userId, ChangePasswordRequest request) {
    User user = userRepository
      .findById(userId)
      .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (
      !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())
    ) {
      throw new BadCredentialsException("Current password is incorrect");
    }

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
  }

  private String blankToNull(String s) {
    if (s == null) return null;
    String t = s.trim();
    return t.isEmpty() ? null : t;
  }

  private UserResponse toResponse(User u) {
    return UserResponse.builder()
      .id(u.getId())
      .name(u.getName())
      .email(u.getEmail())
      .phone(u.getPhone())
      .avatarUrl(u.getAvatarUrl())
      .role(u.getRole().name())
      .isActive(u.getIsActive())
      .createdAt(u.getCreatedAt())
      .gender(u.getGender() != null ? u.getGender().name() : null)
      .currentAddress(u.getCurrentAddress())
      .city(u.getCity())
      .state(u.getState())
      .country(u.getCountry())
      .pincode(u.getPincode())
      .build();
  }
}
