package com.softwareuniverse.controller;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import com.softwareuniverse.common.response.ApiResponse;
import com.softwareuniverse.dto.request.ChangePasswordRequest;
import com.softwareuniverse.dto.request.UpdateProfileRequest;
import com.softwareuniverse.dto.response.UserResponse;
import com.softwareuniverse.entity.User;
import com.softwareuniverse.repository.UserRepository;
import com.softwareuniverse.service.UserService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserProfileController {

  private final UserService userService;
  private final UserRepository userRepository;

  @GetMapping
  public ResponseEntity<ApiResponse<UserResponse>> getProfile(Principal principal) {
    return ResponseEntity.ok(
        ApiResponse.success("Profile fetched", userService.getProfile(currentUserId(principal))));
  }

  @PutMapping
  public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
      Principal principal, @Valid @RequestBody UpdateProfileRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            "Profile updated", userService.updateProfile(currentUserId(principal), request)));
  }

  @PostMapping("/change-password")
  public ResponseEntity<ApiResponse<Void>> changePassword(
      Principal principal, @Valid @RequestBody ChangePasswordRequest request) {
    userService.changePassword(currentUserId(principal), request);
    return ResponseEntity.ok(ApiResponse.success("Password changed", null));
  }

  private Long currentUserId(Principal principal) {
    if (principal == null) throw new ResourceNotFoundException("Unauthorized");
    User user =
        userRepository
            .findByEmail(principal.getName())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return user.getId();
  }
}