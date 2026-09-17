package com.softwareuniverse.service;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import com.softwareuniverse.dto.request.AdminUserUpdateRequest;
import com.softwareuniverse.dto.response.UserResponse;
import com.softwareuniverse.entity.Role;
import com.softwareuniverse.entity.User;
import com.softwareuniverse.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

  private final UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  public Page<UserResponse> getAllUsers(int page, int size, String search) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<User> users = userRepository.findAll(pageable);

    if (search != null && !search.isBlank()) {
      String q = search.toLowerCase();
      List<User> filtered =
          users.getContent().stream()
              .filter(
                  u ->
                      u.getName().toLowerCase().contains(q)
                          || u.getEmail().toLowerCase().contains(q)
                          || (u.getPhone() != null && u.getPhone().contains(search)))
              .toList();
      return new PageImpl<>(filtered.stream().map(this::toResponse).toList(), pageable, filtered.size());
    }

    return users.map(this::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public UserResponse getUser(Long id) {
    User u =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return toResponse(u);
  }

  @Override
  @Transactional
  public UserResponse updateUser(Long id, AdminUserUpdateRequest request) {
    User u =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (request.getName() != null && !request.getName().isBlank()) u.setName(request.getName());
    if (request.getPhone() != null) u.setPhone(request.getPhone());

    if (request.getRole() != null && !request.getRole().isBlank()) {
      try {
        u.setRole(Role.valueOf(request.getRole().toUpperCase()));
      } catch (Exception e) {
        throw new RuntimeException("Invalid role. Use USER or ADMIN.");
      }
    }

    if (request.getIsActive() != null) u.setIsActive(request.getIsActive());

    userRepository.save(u);
    return toResponse(u);
  }

  @Override
  @Transactional
  public UserResponse toggleActive(Long id) {
    User u =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    u.setIsActive(!Boolean.TRUE.equals(u.getIsActive()));
    userRepository.save(u);
    return toResponse(u);
  }

  @Override
  @Transactional
  public void deleteUser(Long id) {
    User u =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (u.getRole() == Role.ADMIN) {
      long adminCount = userRepository.countByRole(Role.ADMIN);
      if (adminCount <= 1) {
        throw new RuntimeException("Cannot delete the last admin user");
      }
    }

    userRepository.delete(u);
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