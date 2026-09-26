package com.softwareuniverse.service;

import com.softwareuniverse.dto.request.ChangePasswordRequest;
import com.softwareuniverse.dto.request.UpdateProfileRequest;
import com.softwareuniverse.dto.response.UserResponse;

public interface UserService {
  UserResponse getProfile(Long userId);

  UserResponse updateProfile(Long userId, UpdateProfileRequest request);

  void changePassword(Long userId, ChangePasswordRequest request);
}
