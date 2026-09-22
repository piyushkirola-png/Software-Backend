package com.softwareuniverse.controller;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import com.softwareuniverse.common.response.ApiResponse;
import com.softwareuniverse.dto.request.ChangePasswordRequest;
import com.softwareuniverse.dto.request.UpdateProfileRequest;
import com.softwareuniverse.dto.response.UserResponse;
import com.softwareuniverse.entity.User;
import com.softwareuniverse.repository.UserRepository;
import com.softwareuniverse.service.AvatarStorageService;
import com.softwareuniverse.service.UserService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserProfileController {

  private final UserService userService;
  private final UserRepository userRepository;
  private final AvatarStorageService avatarStorageService;

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

  @PostMapping("/avatar")
  public ResponseEntity<ApiResponse<UserResponse>> uploadAvatar(
      Principal principal, @RequestParam("file") MultipartFile file) {
    Long userId = currentUserId(principal);
    String url = avatarStorageService.store(file, userId);

    UpdateProfileRequest req = new UpdateProfileRequest();
    req.setAvatarUrl(url);

    UserResponse updated = userService.updateProfile(userId, req);
    return ResponseEntity.ok(ApiResponse.success("Avatar updated", updated));
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
