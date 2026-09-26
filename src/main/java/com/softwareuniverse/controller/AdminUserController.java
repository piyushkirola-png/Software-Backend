package com.softwareuniverse.controller;

import com.softwareuniverse.common.response.ApiResponse;
import com.softwareuniverse.dto.request.AdminUserUpdateRequest;
import com.softwareuniverse.dto.response.UserResponse;
import com.softwareuniverse.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

  private final AdminUserService adminUserService;

  @GetMapping
  public ResponseEntity<ApiResponse<Page<UserResponse>>> getAll(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size,
    @RequestParam(required = false) String search
  ) {
    return ResponseEntity.ok(
      ApiResponse.success(
        "Users fetched",
        adminUserService.getAllUsers(page, size, search)
      )
    );
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<UserResponse>> get(@PathVariable Long id) {
    return ResponseEntity.ok(
      ApiResponse.success("User fetched", adminUserService.getUser(id))
    );
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<UserResponse>> update(
    @PathVariable Long id,
    @RequestBody AdminUserUpdateRequest request
  ) {
    return ResponseEntity.ok(
      ApiResponse.success(
        "User updated",
        adminUserService.updateUser(id, request)
      )
    );
  }

  @PostMapping("/{id}/toggle-active")
  public ResponseEntity<ApiResponse<UserResponse>> toggle(
    @PathVariable Long id
  ) {
    return ResponseEntity.ok(
      ApiResponse.success("User toggled", adminUserService.toggleActive(id))
    );
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
    adminUserService.deleteUser(id);
    return ResponseEntity.ok(ApiResponse.success("User deleted", null));
  }
}
