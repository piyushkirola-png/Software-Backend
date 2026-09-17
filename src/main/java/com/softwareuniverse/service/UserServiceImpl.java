package com.softwareuniverse.service;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import com.softwareuniverse.dto.request.ChangePasswordRequest;
import com.softwareuniverse.dto.request.UpdateProfileRequest;
import com.softwareuniverse.dto.response.UserResponse;
import com.softwareuniverse.entity.User;
import com.softwareuniverse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional(readOnly = true)
  public UserResponse getProfile(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return toResponse(user);
  }

  @Override
  @Transactional
  public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (request.getName() != null && !request.getName().isBlank()) user.setName(request.getName());
    if (request.getPhone() != null) user.setPhone(request.getPhone());
    if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());

    userRepository.save(user);
    return toResponse(user);
  }

  @Override
  @Transactional
  public void changePassword(Long userId, ChangePasswordRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
      throw new BadCredentialsException("Current password is incorrect");
    }

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
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
        .build();
  }
}