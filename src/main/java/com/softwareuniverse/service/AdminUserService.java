package com.softwareuniverse.service;

import com.softwareuniverse.dto.request.AdminUserUpdateRequest;
import com.softwareuniverse.dto.response.UserResponse;
import org.springframework.data.domain.Page;

public interface AdminUserService {

  Page<UserResponse> getAllUsers(int page, int size, String search);

  UserResponse getUser(Long id);

  UserResponse updateUser(Long id, AdminUserUpdateRequest request);

  UserResponse toggleActive(Long id);

  void deleteUser(Long id);
}
